package com.denso.geko.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.denso.geko.domain.LocationDomain;

@Repository
@Mapper
public interface LocationRepository {
    
    /**
     * 指定したBeaconTXのID、日時での経路情報を取得する
     * @param btxId BeaconTXのID
     * @param fromTime 開始日時(UNIX time)
     * @param toTime 終了日時(UNIX time)
     * @return 経路情報
     */
    @Select("SELECT "
            + "a.btx_id as btxId,"
            + "a.recv_timestamp as RecvTimestamp,"
            + "a.major,"
            + "a.minor,"
            + "a.pos_id as posId,"
            + "a.beacon_id as beaconId,"
            + "b.lat,"
            + "b.lon"
            + " FROM geko.td_location a"
            + " INNER JOIN geko.tm_point b"
            + " ON b.id = a.beacon_id"
            + " AND b.invalid = FALSE"
            + " WHERE a.btx_id  = #{btxId}"
            + " AND a.recv_timestamp BETWEEN #{fromTime} AND #{toTime}"
            + " ORDER BY a.recv_timestamp ASC")
    public List<LocationDomain> search(@Param("btxId")int btxId, @Param("fromTime")long fromTime, @Param("toTime")long toTime);
    
    /**
     * 指定したBeaconTXのID、日付の下校開始日時(UNIX time)を取得する
     * ※下校開始日時・・・13:00以降で学校ビーコンを最初に受信した時間
     * @param btxId BeaconTXのID
     * @param fromTime 開始日時(UNIX time)
     * @param toTime　終了日時(UNIX time)
     * @return 下校開始日時(UNIX time)
     */
    @Select("SELECT min(recv_timestamp) FROM geko.td_location"
            + " WHERE btx_id = #{btxId}"
            + " AND recv_timestamp BETWEEN #{fromTime} AND #{toTime}"
            + " AND beacon_id IN (SELECT id FROM geko.tm_point WHERE type = '0' AND invalid = FALSE)")
    public Long getFromTime(@Param("btxId")int btxId, @Param("fromTime")long fromTime, @Param("toTime")long toTime);
    
    /**
     * 指定したBeaconTXのID、日付の登校終了日時(UNIX time)を取得する
     * ※登校終了日時・・・9:00以前で学校ビーコンを最後に受信した時間
     * @param btxId BeaconTXのID
     * @param fromTime 開始日時(UNIX time)
     * @param toTime　終了日時(UNIX time)
     * @return 登校終了日時(UNIX time)
     */
    @Select("SELECT max(recv_timestamp) FROM geko.td_location"
            + " WHERE btx_id = #{btxId}"
            + " AND recv_timestamp BETWEEN #{fromTime} AND #{toTime}"
            + " AND beacon_id IN (SELECT id FROM geko.tm_point WHERE type = '0' AND invalid = FALSE)")
    public Long getToTime(@Param("btxId")int btxId, @Param("fromTime")long fromTime, @Param("toTime")long toTime);

    /**
     * 指定したBeaconTXのID、日時での経路情報を取得する(CSVファイルダウンロード用)
     * @param btxId BeaconTXのID
     * @param fromTime 開始日時(UNIX time)
     * @param toTime 終了日時(UNIX time)
     * @return 経路情報
     */
    @Select("SELECT "
            + "a.btx_id as btxId,"
            + "a.recv_timestamp as RecvTimestamp,"
            + "a.major,"
            + "a.minor,"
            + "a.pos_id as posId,"
            + "a.beacon_id as beaconId,"
            + "b.lat,"
            + "b.lon,"
            + "TO_TIMESTAMP(a.recv_timestamp / 1000) as recvDate"
            + " FROM geko.td_location a"
            + " INNER JOIN geko.tm_point b"
            + " ON b.id = a.beacon_id"
            + " AND b.invalid = FALSE"
            + " WHERE a.btx_id  = #{btxId}"
            + " AND a.recv_timestamp BETWEEN #{fromTime} AND #{toTime}"
            + " ORDER BY a.recv_timestamp ASC")
    public List<LocationDomain> download(@Param("btxId")int btxId, @Param("fromTime")long fromTime, @Param("toTime")long toTime);
    
    /**
     * データベースから対象児童のタグIDの最後に検出したビーコンの座標を取得する
     * @param userId 児童のユーザID
     * @param fromtime　現在日時の5分前(UNIX time)
     * @return 現在地情報
     */
    @Select("SELECT "
            + "b.btx_id as btxId,"
            + "b.pos_id as posId,"
            + "b.beacon_id as beaconId,"
            + "b.recv_timestamp as recvTimestamp,"
            + "c.lat,"
            + "c.lon"
            + " FROM geko.td_location b"
            + " INNER JOIN geko.tm_point c"
            + " ON c.id = b.beacon_id"
            + " AND c.invalid = FALSE"
            + " WHERE b.btx_id = (SELECT a.btx_id FROM geko.tm_user a WHERE a.user_id = #{userId})"
            + " AND b.recv_timestamp = (SELECT MAX(recv_timestamp) FROM geko.td_location d"
            + " WHERE d.btx_id = b.btx_id"
            + " AND d.recv_timestamp > #{fromtime})")
    public LocationDomain getLocation(@Param("userId")String userId, @Param("fromtime")long fromtime);

}