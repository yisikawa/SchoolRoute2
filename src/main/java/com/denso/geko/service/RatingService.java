package com.denso.geko.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.denso.geko.domain.GeometryDomain;
import com.denso.geko.domain.LocationDomain;
import com.denso.geko.domain.RatingDomain;
import com.denso.geko.repository.RatingRepository;

@Service
@Transactional
public class RatingService {
    
    @Autowired
    private RatingRepository ratingRepository;
    
    /**
     * 評価情報を取得する
     * @param domain 評価情報の検索条件
     * @return 評価情報リスト
     */
    public List<RatingDomain> getRating(RatingDomain domain) {
        
        // 評価情報を取得
        return ratingRepository.search(domain);
    }
    
    /**
     * 評価情報の評価値を更新する
     * @param domains 評価値のリスト
     * @return 更新件数
     */
    public int updateRatingOnly(List<RatingDomain>domains) {
        int updCnt = 0;
        for (RatingDomain domain: domains) {
            // 評価値を更新する
            if (ratingRepository.updateRatingOnly(domain) == 0) {
                ratingRepository.insertRatingOnly(domain);
            }
            updCnt++;
        }
        return updCnt;
    }
    
    /**
     * 経路情報、通学路情報より評価の参考値を算出し、評価情報を登録する
     * @param cuserId 児童のユーザID
     * @param userId 保護者のユーザID
     * @param routes 経路情報
     * @param schoolroutes 通学路情報
     * @return 登録件数
     */
    public int create(
            String cuserId,
            String userId,
            List<LocationDomain>routes,
            List<GeometryDomain>schoolroutes) {
        int insertCnt = 0;
        
        if (routes.size() > 0) {
            // 経路情報より補正情報を除去し、BeaconIdのリストに変換する
            List<Integer> rt = routes.stream()
                    .filter(r -> r.getBeaconId() != 0)
                    .map(r -> r.getBeaconId())
                    .collect(Collectors.toList());
            // 経路に存在する通学路上のBeaconIdをMapに変換する
            Map<Integer, Integer> sr = new HashMap<Integer, Integer>();
            for (int i = 0; i < schoolroutes.size(); i++) {
                int beaconId = schoolroutes.get(i).getId();
                if (rt.contains(beaconId)) {
                    sr.put(beaconId, i);
                }
            }
            
            //　近道の件数
            int shortCutCnt = schoolroutes.size() - sr.size();
            
            // 寄り道、逆走の件数
            int detourCnt = 0;
            int wrongWayCnt = 0;
            for (int i = 0; i < rt.size(); i++) {
                if (sr.containsKey(rt.get(i))) {
                    // 経路の1つ前のポイントと通学路の1つ後のポイントのBeaconIdが一致する場合逆走とする
                    int srIdx = sr.get(rt.get(i));
                    if (i > 0 && srIdx < schoolroutes.size() - 2
                            && rt.get(i - 1) == schoolroutes.get(srIdx + 1).getId()) {
                        wrongWayCnt++;
                    }
                } else {
                    // 通学路に含まれない交差点は寄り道とする
                    detourCnt++;
                }
            }
            
            // 経路の最初と最後の受信時間の差を所要時間とする
            long fromtime = routes.get(0).getRecvTimestamp();
            long totime = routes.get(routes.size() - 1).getRecvTimestamp();
            int elapsedTime = (int)(totime - fromtime) / (1000 * 60);
            
            // 評価情報を登録する
            Date ratingDate = new Date(fromtime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            RatingDomain criteria = new RatingDomain();
            criteria.setCuserId(cuserId);
            criteria.setRatingDate(sdf.format(ratingDate));
            RatingDomain rating = ratingRepository.find(criteria);
            if (rating == null) {
                rating = criteria;
            }
            rating.setShortCutCnt(shortCutCnt);
            rating.setDetourCnt(detourCnt);
            rating.setWrongWayCnt(wrongWayCnt);
            rating.setElapsedTime(elapsedTime);
            rating.setUserId(userId);
            
            if (rating.getRating() == null) {
                rating.setRating(0);
                insertCnt += ratingRepository.insert(rating);
            } else {
                insertCnt += ratingRepository.update(rating);
            }
        }
        
        return insertCnt;
    }
}
