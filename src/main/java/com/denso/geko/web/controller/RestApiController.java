package com.denso.geko.web.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.geojson.geometry.LineString;
import org.geojson.geometry.Point;
import org.geojson.object.Feature;
import org.geojson.object.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.denso.geko.domain.GeometryDomain;
import com.denso.geko.domain.LocationDomain;
import com.denso.geko.domain.RatingDomain;
import com.denso.geko.domain.UserDomain;
import com.denso.geko.service.LocationService;
import com.denso.geko.service.MarkerService;
import com.denso.geko.service.RatingService;
import com.denso.geko.service.RouteService;
import com.denso.geko.service.UserService;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;

@RestController
@RequestMapping(value="/api")
@Validated
public class RestApiController {

    private static String TOGEKO_TYPE_GEKO = "2";

    @Autowired
    private UserService userService;
    
    @Autowired
    private RouteService routeService;
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private MarkerService markerService;
    
    @Autowired
    private RatingService ratingService;
    
    /**
     * ユーザが参照可能な児童のユーザ情報を取得する
     * @param auth 権限情報
     * @return 児童のユーザ情報リスト
     */
    @RequestMapping(value="/auth", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserDomain> getAuth(Authentication auth) {
        
        // リクエストより認証したユーザIDを取得
                String userId = auth.getPrincipal().toString();
        
        // 参照権限がある児童の情報を取得
        List<UserDomain>userList = userService.searchAuthList(userId);
        
        return userList;
    }
    
    /**
     * 指定した児童の通学路を取得する
     * @param response レスポンス
     * @param auth 権限情報
     * @param cuserId 児童のユーザID
     * @return 通学路情報(GeoJSON形式)
     */
    @RequestMapping(value="/schoolroute", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FeatureCollection getSchoolRoute(
            HttpServletResponse response,
            Authentication auth,
            @RequestParam(name = "userid") String cuserId) {
        
        // 指定した児童の権限有無を確認
        if (!userService.chkAuth(auth.getPrincipal().toString(), cuserId)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }
        
        // 通学路を取得
        List<GeometryDomain> schoolroutes = routeService.getSchoolRoute(cuserId);

        // GeoJSON形式に変換
        List<Point> points = schoolroutes.stream()
                .map(r -> new Point(r.getLon(), r.getLat()))
                .collect(Collectors.toList());
        Feature feature = new Feature(new LineString(points));
        List<Feature> features = new ArrayList<>();
        features.add(feature);
        FeatureCollection featureCollection = new FeatureCollection(features);
        
        return featureCollection;
    }
    
    /**
     * 指定した児童のユーザID、日付、登下校種別の経路情報を取得する
     * @param response レスポンス
     * @param auth 権限情報
     * @param cuserId 児童のユーザID
     * @param date 日付(yyyyMMdd)
     * @param togeko 登下校種別(1:登校、2:下校)
     * @return 経路情報(GPX形式)
     */
    @RequestMapping(value="/route", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public String getRoute(
            HttpServletResponse response,
            Authentication auth,
            @RequestParam(name = "userid") String cuserId,
            @NotBlank
            @Pattern(regexp = "^([MTSH]\\d{1,2}|\\d{2,4})/?(0?[1-9]|1[0-2])/?(0?[1-9]|[1-2][0-9]|3[0-1])$")
            @RequestParam(name = "date", required = true) String date,
            @NotBlank
            @Pattern(regexp = "^[12]$")
            @RequestParam(name = "type", required = true) String togeko
            ) {
        
        // 指定した児童の権限有無を確認
        if (!userService.chkAuth(auth.getPrincipal().toString(), cuserId)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }
        
        // 測位情報を取得
        List<LocationDomain> routes = routeService.search(cuserId, date, togeko);
        
        // 下校表示の場合、対象経路の評価結果を登録する
        if (TOGEKO_TYPE_GEKO.equals(togeko)) {
            List<GeometryDomain> schoolroutes = routeService.getSchoolRoute(cuserId);
            ratingService.create(cuserId, auth.getPrincipal().toString(), routes, schoolroutes);
        }
        
        // GPX形式に変換
        List<WayPoint> points = routes.stream()
                .map(r -> WayPoint.of(r.getLat(), r.getLon(), r.getRecvTimestamp()))
                .collect(Collectors.toList());
        GPX gpx = GPX.builder()
                .addTrack(track -> track
                        .addSegment(segment -> segment.points(points)))
                .build();
        
        return GPX.writer().toString(gpx);
    }
    
    /**
     * 指定した児童の現在地を取得する
     * @param response レスポンス
     * @param auth 権限情報
     * @param cuserId 児童のユーザID
     * @return 現在地情報(GeoJSON形式)
     */
    @RequestMapping(value="/location", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FeatureCollection getLocation(
            HttpServletResponse response,
            Authentication auth,
            @RequestParam(name = "userid") String cuserId) {
        
        // 指定した児童の権限有無を確認
        if (!userService.chkAuth(auth.getPrincipal().toString(), cuserId)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }
        
        // 現在地を取得
        LocationDomain location = locationService.getLocation(cuserId);

        // GeoJSON形式に変換
        FeatureCollection featureCollection = null;
        if (location != null) {
            Point point = new Point();
            double[] coordinates = {location.getLon(), location.getLat()};
            point.setCoordinates(coordinates);
            Feature feature = new Feature(point);
            List<Feature> features = new ArrayList<>();
            features.add(feature);
            featureCollection = new FeatureCollection(features);
        }
        
        return featureCollection;
    }
    
    /**
     * 児童の評価情報を取得する
     * @param response レスポンス
     * @param auth 権限情報
     * @param cuserId 児童のユーザID
     * @param date 日付(yyyyMMdd)
     * @return 児童の評価情報リスト(JSON形式)
     */
    @RequestMapping(value="/rating", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RatingDomain> getRating(
            HttpServletResponse response,
            Authentication auth,
            @RequestParam(name = "userid") String cuserId,
            @NotBlank
            @Pattern(regexp = "^([MTSH]\\d{1,2}|\\d{2,4})/?(0?[1-9]|1[0-2])/?(0?[1-9]|[1-2][0-9]|3[0-1])$")
            @RequestParam(name = "date", required = true) String date) {
        
        // 指定した児童の権限有無を確認
        String userId = auth.getPrincipal().toString();
        if (!userService.chkAuth(userId, cuserId)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }
        
        // 対象日の週の日曜日の日付を取得する
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(date.substring(0, 4)),
                Integer.parseInt(date.substring(4, 6)) - 1,
                Integer.parseInt(date.substring(6, 8)),
                0, 0, 0);
        cal.add(Calendar.DATE, -(cal.get(Calendar.DAY_OF_WEEK) - 1));
        
        // 1週間分の評価結果を取得する
        RatingDomain criteria = new RatingDomain();
        criteria.setUserId(userId);
        criteria.setCuserId(cuserId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        criteria.setRatingDate(sdf.format(cal.getTime()));
        List<RatingDomain>ratings = ratingService.getRating(criteria);
        
        // 評価データがない日が存在する場合、空の結果を追加する
        for(int i = 0; i < 7; i++) {
            if (ratings.size() < i + 1
                    || !sdf.format(cal.getTime()).equals(ratings.get(i).getRatingDate())) {
                RatingDomain rating = new RatingDomain();
                rating.setRating(0);
                rating.setCuserId(cuserId);
                rating.setRatingDate(sdf.format(cal.getTime()));
                ratings.add(i, rating);
            } else if (ratings.get(i).getRating() == null) {
                ratings.get(i).setRating(0);
            }
            cal.add(Calendar.DATE, 1);
        }
        
        // 週間の評価を算出する
        // 地図メモの件数は土日の登録数も含める
        // 近道・寄り道・逆走の平均と評価値の合計は月から金のみを対象とする
        RatingDomain ratingSum = new RatingDomain();
        for (int i = 0; i < ratings.size(); i++) {
            RatingDomain rating = ratings.get(i);
            if (i >= 1 && i <= 5) {
                ratingSum.setRating(sum(ratingSum.getRating(), rating.getRating()));
                ratingSum.setShortCutCnt(sum(ratingSum.getShortCutCnt(), rating.getShortCutCnt()));
                ratingSum.setDetourCnt(sum(ratingSum.getDetourCnt(), rating.getDetourCnt()));
                ratingSum.setWrongWayCnt(sum(ratingSum.getWrongWayCnt(), rating.getWrongWayCnt()));
                ratingSum.setElapsedTime(sum(ratingSum.getElapsedTime(), rating.getElapsedTime()));
            }
            ratingSum.setMarkerCnt(ratingSum.getMarkerCnt() + rating.getMarkerCnt());
        }
        // 日曜の評価結果を週間評価に置き換え
        ratings.set(0, ratingSum);
        // 土曜の結果を削除
        ratings.remove(6);
        
        return ratings;
    }
    
    /**
     * 評価結果を更新する
     * @param response レスポンス
     * @param auth 権限情報
     * @param ratings 評価情報リスト
     * @return 更新件数
     */
    @RequestMapping(value="/rating", method=RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public int updateRating(
            HttpServletResponse response,
            Authentication auth,
            @RequestBody List<RatingDomain> ratings) {
        
        String userId = auth.getPrincipal().toString();

        // 評価日がnull(評価対象外)を除外し、保護者のユーザIDを設定する
        List<RatingDomain>updateRatings = ratings.stream()
//                .filter(r -> r.getCuserId() != null)
                .map(r -> {
                    r.setUserId(userId);
                    return r;
                })
                .collect(Collectors.toList());
        
        int updCnt = 0;
        if (updateRatings.size() > 0) {
            // 指定した児童の権限有無を確認
            if (!userService.chkAuth(userId, updateRatings.get(0).getCuserId())) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
            } else {
                // 評価結果を更新する
                updCnt = ratingService.updateRatingOnly(updateRatings);
            }
        }
        return updCnt;
    }

    /**
     * 地図上に表示するマーカー情報を取得する
     * @param response レスポンス
     * @param auth 権限情報
     * @return マーカー情報(GeoJSON形式)
     */
    @RequestMapping(value="/marker", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FeatureCollection getMarker(
            HttpServletResponse response,
            Authentication auth) {
        
        String userId = auth.getPrincipal().toString();
        
        // マーカー情報を取得する
        List<GeometryDomain> markers = markerService.getMarker(userId);

        // GeoJSON形式に変換
        List<Feature>features = markers.stream()
                .map(m -> {
                    Feature feature = new Feature(new Point(m.getLon(), m.getLat()));
                    Map<String, Serializable>prop = new HashMap<>();
                    prop.put("id", m.getId());
                    prop.put("type", m.getMarkerType());
                    prop.put("icon", m.getMarkerIcon());
                    prop.put("comment", m.getMarkerComment());
                    feature.setProperties(prop);
                    return feature;
                })
                .collect(Collectors.toList());
        FeatureCollection featureCollection = new FeatureCollection(features);
        
        return featureCollection;
    }
    
    /**
     * マーカー情報を登録する
     * @param auth 権限情報
     * @param marker マーカー情報
     * @return 登録したマーカーのid
     */
    @RequestMapping(value="/marker", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public int createMarker(
            Authentication auth,
            @RequestBody GeometryDomain marker) {
        
        marker.setUserId(auth.getPrincipal().toString());
        
        // 新規マーカーを登録する
        return markerService.create(marker);
    }

    /**
     * マーカー情報を更新する
     * @param auth 権限情報
     * @param marker マーカー情報
     * @return 更新件数
     */
    @RequestMapping(value="/marker", method=RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public int updateMarker(
            Authentication auth,
            @RequestBody GeometryDomain marker) {
        
        marker.setUserId(auth.getPrincipal().toString());
        
        // マーカーを更新する
        return markerService.update(marker);
    }

    /**
     * マーカー情報を削除する
     * @param auth 権限情報
     * @param id 削除対象のマーカーのid
     * @return 削除件数
     */
    @RequestMapping(value="/marker", method=RequestMethod.DELETE)
    public int deleteMarker(
            Authentication auth,
            @RequestParam(name = "id") int id) {
        
        GeometryDomain marker = new GeometryDomain();
        marker.setId(id);
        marker.setUserId(auth.getPrincipal().toString());
        
        // マーカーを削除する
        return markerService.delete(marker);
    }
    
    /**
     * Integerの値を加算する
     * @param num1 値1
     * @param num2 値2
     * @return 両方null以外：加算した値、いずれかがnull：null以外の値、両方null：null.
     */
    private Integer sum(Integer num1, Integer num2) {
        Integer num = null;
        if (num1 != null && num2 != null) {
            num = num1 + num2;
        } else if (num1 != null && num2 == null) {
            num = num1;
        } else if (num1 == null && num2 != null) {
            num = num2;
        }
        
        return num;
    }

}
