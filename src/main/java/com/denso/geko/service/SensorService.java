package com.denso.geko.service;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.denso.geko.domain.SensorDomain;
import com.denso.geko.repository.SensorRepository;

@Service
@Transactional
public class SensorService {
    
    @Autowired
    private SensorRepository sensorRepository;

    /**
     * センサーデータを登録する
     * @param sensors センサーリスト
     * @return 登録件数
     */
    public int insert(List<SensorDomain>sensors) {

        // 既存データを削除する
        sensorRepository.delete(
                sensors.get(0).getSensorId(),
                sensors.get(0).getRecvTimestamp(),
                sensors.get(sensors.size() - 1).getRecvTimestamp());
        
        // 1000件ずつINSERT
        int insertSize = 1000;
        for (int i = 0; i < Math.ceil((double)sensors.size() / insertSize); i++) {
            int fromIndex = i * insertSize;
            int toIndex = Math.min(fromIndex + insertSize, sensors.size());
            sensorRepository.insertAll(sensors.subList(fromIndex, toIndex));
        }
        return sensors.size();
    }
    
    /**
     * 指定したBeaconTXのID、日時でのセンサー情報を取得する
     * @param btxId 児童のタグID
     * @param date 日付
     * @return センサー情報リスト
     */
    public List<SensorDomain> download(int btxId, String date) {
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
        
        // センサー情報を取得する
        List<SensorDomain> routes = sensorRepository.download(btxId, fromTime, toTime);
        
        return routes;
    }

}
