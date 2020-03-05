package com.denso.geko.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.denso.geko.domain.SensorDomain;

@Repository
@Mapper
public interface SensorRepository {
    
    /**
     * sensorデータを登録する
     * @param sensors 登録情報リスト
     * @return 登録件数
     */
    @Insert("<script>"
            + "INSERT INTO geko.td_sensor"
            + "(sensor_id,recv_timestamp,"
            + "accl_x,accl_y,accl_z,angl_x,angl_y,angl_z,"
            + "temperature,pressure) VALUES"
            + "<foreach item='item' collection='sensors' separator=','>"
            + "('${item.sensorId}',${item.recvTimestamp},"
            + "TO_NUMBER('${item.acclX}', '9D9999999999'),TO_NUMBER('${item.acclY}', '9D9999999999'),TO_NUMBER('${item.acclZ}', '9D9999999999'),"
            + "TO_NUMBER('${item.anglX}', '9999D9999999999'),TO_NUMBER('${item.anglY}', '9999D9999999999'),TO_NUMBER('${item.anglZ}', '9999D9999999999'),"
            + "TO_NUMBER('${item.temperature}', '99D99'),TO_NUMBER('${item.pressure}', '9999D9999'))"
            + "</foreach>"
            + "</script>")
    public int insertAll(@Param("sensors") List<SensorDomain> sensors);
    
    /**
     * 登録するセンサーデータとセンサーID、期間が重複するデータを削除する
     * @param sensorId センサーID
     * @param fromTimestamp 検知時刻(From)
     * @param toTimestamp 検知時刻(To)
     * @return 削除件数
     */
    @Delete("DELETE FROM geko.td_sensor"
            + " WHERE sensor_Id = #{sensorId}"
            + " AND recv_timestamp between #{fromTimestamp} and #{toTimestamp}")
    public int delete(@Param("sensorId")String sensorId, @Param("fromTimestamp")long fromTimestamp, @Param("toTimestamp")long toTimestamp);

    /**
     * 指定したBeaconTXのID、日時でのセンサー情報を取得する
     * @param btxId BeaconTXのID
     * @param fromTime 開始日時(UNIX time)
     * @param toTime 終了日時(UNIX time)
     * @return センサー情報リスト
     */
    @Select("SELECT "
            + "a.sensor_id as sensorId,"
            + "recv_timestamp as recvTimestamp,"
            + "accl_x as acclX,"
            + "accl_y as acclY,"
            + "accl_z as acclZ,"
            + "angl_x as anglX,"
            + "angl_y as anglY,"
            + "angl_z as anglZ,"
            + "temperature,"
            + "pressure,"
            + "TO_TIMESTAMP(a.recv_timestamp / 1000) as time"
            + " FROM geko.td_sensor a"
            + " WHERE a.sensor_id IN ("
            + " SELECT sensor_id FROM geko.tm_user"
            + " WHERE btx_id = #{btxId}"
            + " UNION ALL"
            + " SELECT sensor2_id FROM geko.tm_user"
            + " WHERE btx_id = #{btxId})"
            + " AND a.recv_timestamp BETWEEN #{fromTime} AND #{toTime}"
            + " ORDER BY a.recv_timestamp ASC")
    public List<SensorDomain> download(@Param("btxId")int btxId, @Param("fromTime")long fromTime, @Param("toTime")long toTime);
    

}
