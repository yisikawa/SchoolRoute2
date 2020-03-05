package com.denso.geko.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.denso.geko.domain.GeometryDomain;
import com.denso.geko.domain.LocationDomain;
import com.denso.geko.domain.UserDomain;
import com.denso.geko.repository.GeometryRepository;
import com.denso.geko.repository.LocationRepository;
import com.denso.geko.repository.UserRepository;

@Service
@Transactional
public class RouteService {
    
    private static String TOGEKO_TYPE_TOKO = "1";
    private static String TOGEKO_TYPE_GEKO = "2";
    
    @Autowired
    private GeometryRepository geometryRepository;

    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 児童の通学路を取得する
     * @param userId 児童のユーザID
     * @return 通学路情報
     */
    public List<GeometryDomain> getSchoolRoute(String userId) {
        return geometryRepository.getSchoolRoute(userId);
    }

    /**
     * 指定したBeaconTXのID、日時での経路情報を取得する
     * @param cuserId 児童のユーザID
     * @param date 日付
     * @param type 登下校種別
     * @return 経路情報リスト
     */
    public List<LocationDomain> search(String cuserId, String date, String type) {
        
        // EXBeaconなしの交差点
        // XXX 今回の実証実験は対象交差点を決め打ちで定義。エリアを広げる場合はDB保持に変更する必要あり
        Map<String, Integer>noBeacon = new HashMap<String, Integer>();
        noBeacon.put("40987-41015", 45112);     // No.27-55
        noBeacon.put("40987-41017", 45112);     // No.27-57
        noBeacon.put("41015-40987", 45112);     // No.55-27
        noBeacon.put("41015-41017", 45112);     // No.55-57
        noBeacon.put("41017-40987", 45112);     // No.57-27
        noBeacon.put("41017-41015", 45112);     // No.57-55
        noBeacon.put("40999-41045", 45146);     // No.39-85
        noBeacon.put("40999-41049", 45146);     // No.39-89
        noBeacon.put("41045-40999", 45146);     // No.85-39
        noBeacon.put("41045-41049", 45146);     // No.85-89
        noBeacon.put("41049-40999", 45146);     // No.89-39
        noBeacon.put("41049-41045", 45146);     // No.89-85
        noBeacon.put("41009-41011", 41010);     // No.49-51
        noBeacon.put("41009-41037", 41010);     // No.49-77
        noBeacon.put("41011-41009", 41010);     // No.51-49
        noBeacon.put("41011-41037", 41010);     // No.51-77
        noBeacon.put("41037-41009", 41010);     // No.77-49
        noBeacon.put("41037-41011", 41010);     // No.77-51
        noBeacon.put("41023-41025", 41024);     // No.63-65
        noBeacon.put("41023-40993", 41024);     // No.63-33
        noBeacon.put("41023-41032", 41024);     // No.63-72
        noBeacon.put("41025-41023", 41024);     // No.65-63
        noBeacon.put("41025-40993", 41024);     // No.65-33
        noBeacon.put("41025-41032", 41024);     // No.65-72
        noBeacon.put("40993-41023", 41024);     // No.33-63
        noBeacon.put("40993-41025", 41024);     // No.33-65
        noBeacon.put("40993-41032", 41024);     // No.33-72
        noBeacon.put("41032-41023", 41024);     // No.72-63
        noBeacon.put("41032-41025", 41024);     // No.72-65
        noBeacon.put("41032-40993", 41024);     // No.72-33
        noBeacon.put("41030-41036", 41035);     // No.70-76
        noBeacon.put("41036-41030", 41035);     // No.76-70
        
        // 児童のBeaconTXのIDを取得する
        UserDomain user = userRepository.find(cuserId);
        
        // 登下校の開始、終了時間を取得する
        int calYear = Integer.parseInt(date.substring(0, 4));
        int calMonth = Integer.parseInt(date.substring(4, 6)) - 1;
        int calDate = Integer.parseInt(date.substring(6, 8));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
        cal.set(calYear, calMonth, calDate, 0, 0, 0);
        cal.clear(Calendar.MILLISECOND);
        // 登下校判定修正
        long fromTime = 0;
        long toTime = 0;
        if (TOGEKO_TYPE_TOKO.equals(type)) {
            // 登校時間(9:00以前で最後に学校ビーコンを検出した時間)を取得する。
            fromTime = cal.getTimeInMillis();
            cal.add(Calendar.HOUR, 9);
            toTime = cal.getTimeInMillis();
            Long time = locationRepository.getToTime(user.getBtxId(), fromTime, toTime);
            if (time != null) {
                toTime = time;
            }
        } else if (TOGEKO_TYPE_GEKO.equals(type)) {
            // 下校開始時間(12：00以降で最初に学校ビーコンを取得した時間)を取得する
            cal.add(Calendar.HOUR, 12);
            fromTime = cal.getTimeInMillis();
            cal.add(Calendar.HOUR, 12);
            toTime = cal.getTimeInMillis() - 1;
            Long time = locationRepository.getFromTime(user.getBtxId(), fromTime, toTime);
            if (time != null) {
                fromTime = time;
            } else {
                fromTime = toTime;
            }
        }
        
        // 経路情報を取得する
        List<LocationDomain> raws = locationRepository.search(user.getBtxId(), fromTime, toTime);
        
        // 経路を補正する
        List<LocationDomain> routes = new ArrayList<LocationDomain>();
        if (raws != null && raws.size() != 0) {
            // 連続して検出したビーコン、2点間の行ったり来たりをリストから除外
            long startTimestamp = raws.get(0).getRecvTimestamp();
            long endTimestamp = raws.get(raws.size() - 1).getRecvTimestamp();
            raws.get(0).setRecvEndTimestamp(startTimestamp);
            routes.add(raws.get(0));
            for (int i = 1; i < raws.size(); i++) {
                LocationDomain next = raws.get(i);
                if (next.getBeaconId() != routes.get(routes.size() - 1).getBeaconId()) {
                    if (routes.size() < 2
                            || next.getBeaconId() != routes.get(routes.size() - 2).getBeaconId()) {
                        // 1つ前、2つ前と違うBeaconIdを検出
                        if (routes.size() > 1 && raws.get(i - 1).getBeaconId() == routes.get(routes.size() - 2).getBeaconId()) {
                            // 対象BeaconIdの直前のBeaconIdが2つ前と一致する場合、2つ前の情報を分割する
                            long preTimestamp = (routes.get(routes.size() - 1).getRecvTimestamp() + routes.get(routes.size() - 1).getRecvEndTimestamp()) / 2;
                            LocationDomain point = raws.get(i - 1);
                            point.setRecvEndTimestamp(point.getRecvTimestamp());
                            point.setRecvTimestamp(preTimestamp);
                            routes.get(routes.size() - 2).setRecvEndTimestamp(preTimestamp);
                            routes.add(point);
                        }
                        next.setRecvEndTimestamp(next.getRecvTimestamp());
                        routes.add(next);
                    } else {
                        // 1つ前とは違い、2つ前と同じBeaconIdを検出（行ったり来たり）
                        routes.get(routes.size() - 2).setRecvEndTimestamp(next.getRecvTimestamp());
                    }
                } else {
                    // 1つ前と同じBeaconIdを検出
                    routes.get(routes.size() - 1).setRecvEndTimestamp(next.getRecvTimestamp());
                    // 最初のBeaconを連続検出した場合、最後に検出した時間を採用する
                    if (routes.size() == 1) {
                        startTimestamp = next.getRecvTimestamp();
                    }
                }
                if (next.getBeaconId() != raws.get(i - 1).getBeaconId()) {
                    endTimestamp = next.getRecvTimestamp();
                }
            }
            
            // 交差点通過時間の設定
            for (int j = 0; j < routes.size(); j++) {
                LocationDomain point = routes.get(j);
                point.setRecvTimestamp((point.getRecvTimestamp() + point.getRecvEndTimestamp()) / 2);
            }
            // 開始と終了の時間を更新（開始：連続検出の最後、終了：連続検出の最初）
            routes.get(0).setRecvTimestamp(startTimestamp);
            routes.get(routes.size() - 1).setRecvTimestamp(endTimestamp);
            
            for (int k = routes.size() - 1; k >= 0; k--) {
                LocationDomain point = routes.get(k);
                
                // 2点間の経路補正
                if (k > 0) {
                    LocationDomain pre = routes.get(k - 1);
                    
                    // EXBeacon検出開始-検出終了時間より交差点通過時間を算出
                    // XXX 家の中から受信し続ける可能性を考慮し、最終レコードは最初に受信した時間を採用する
//                    pre.setRecvTimestamp((pre.getRecvTimestamp() + pre.getRecvEndTimestamp()) / 2);
                    
                    // EXBeacon未設置の交差点にダミー交差点を挿入
                    String key = pre.getBeaconId() + "-" + point.getBeaconId();
                    if (noBeacon.containsKey(key)) {
                        // 対象Beaconの座標を取得する
                        Integer beaconId = noBeacon.get(key);
                        GeometryDomain latlon = geometryRepository.getPoint(beaconId);
                        LocationDomain ap = new LocationDomain();
                        ap.setBeaconId(beaconId);
                        ap.setLat(latlon.getLat());
                        ap.setLon(latlon.getLon());
                        ap.setRecvTimestamp((pre.getRecvTimestamp() + point.getRecvTimestamp()) / 2);
                        routes.add(k, ap);
                        
                        // 2点間の測位情報を補間
                        routes.addAll(k + 1, interpolateRoute(ap, point));
                        point = ap;
                    }
                    // 2点間の測位情報を補間
                    routes.addAll(k, interpolateRoute(pre, point));
                }
            }
            
            // 1点しかない場合（校門の前に家がある等）
            // XXX 経路表示には2点以上必要なので、1点追加する
            if (routes.size() == 1) {
                routes.add(raws.get(raws.size() - 1));
            }
        }
        
        return routes;
    }

    /**
     * 指定したBeaconTXのID、日時での経路情報を取得する
     * @param btxId 児童のタグID
     * @param date 日付
     * @return 経路情報リスト
     */
    public List<LocationDomain> download(int btxId, String date) {
        
        // 登下校の開始、終了時間を取得する
        int calYear = Integer.parseInt(date.substring(0, 4));
        int calMonth = Integer.parseInt(date.substring(4, 6)) - 1;
        int calDate = Integer.parseInt(date.substring(6, 8));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
        cal.set(calYear, calMonth, calDate, 0, 0, 0);
        cal.clear(Calendar.MILLISECOND);
        long fromTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long toTime = cal.getTimeInMillis() - 1;
        
        // 経路情報を取得する
        List<LocationDomain> routes = locationRepository.download(btxId, fromTime, toTime);
        
        return routes;
    }
    /**
      * 2点間の1秒間隔の位置情報を返す
     * @param point1 1点目の地点情報
     * @param point2 2点目の地点情報
     * @return 補間した位置情報リスト
     */
    private List<LocationDomain> interpolateRoute(LocationDomain point1, LocationDomain point2) {
        double x1 = point1.getLon();
        double y1 = point1.getLat();
        long time1 = point1.getRecvTimestamp();
        double x2 = point2.getLon();
        double y2 = point2.getLat();
        long time2 = point2.getRecvTimestamp();
        
        // 2点間の距離と角度を算出
        double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        double radian = Math.atan2(y2 - y1,  x2 - x1);
        
        // 1秒単位で座標を算出
        List<LocationDomain>results = new ArrayList<LocationDomain>();
        long cnt = (time2 - time1) / 1000;
        for (int d = 1; d < cnt; d++) {
            LocationDomain dp = new LocationDomain();
            dp.setLat(y1 + Math.sin(radian) * distance * d / cnt);
            dp.setLon(x1 + Math.cos(radian) * distance * d / cnt);
            dp.setRecvTimestamp(time1 + 1000 * d);
            results.add(dp);
        }
        return results;
    }

}
