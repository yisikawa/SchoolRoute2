package com.denso.geko.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.denso.geko.domain.GeometryDomain;

@Repository
@Mapper
public interface GeometryRepository {
    
    /**
     * 指定した児童の通学路を取得する
     * @param userId 児童のユーザID
     * @return 通学路情報
     */
    @Select("SELECT "
            + "id,"
            + "lon,"
            + "lat"
            + " FROM geko.tm_school_route a"
            + " INNER JOIN geko.tm_point b"
            + " ON b.id = a.beacon_id"
            + " WHERE a.cuser_id  = #{userId}"
            + " AND (b.invalid = false OR b.invalid IS NULL)"
            + " ORDER BY a.route_no ASC")
    public List<GeometryDomain> getSchoolRoute(String userId);

    /**
     * 対象idの緯度・経路を取得
     * @param id 取得対象のid
     * @return 緯度、経度
     */
    @Select("SELECT lon, lat FROM geko.tm_point WHERE id = #{id}")
    public GeometryDomain getPoint(int id);
    
    /**
     * 次の登録要素に付与するIDを取得する
     * EXBeaconで使用するNo.40961以降は除外する
     * @return ID
     */
    @Select("SELECT max(id) + 1 FROM geko.tm_point WHERE id < 40961")
    public int getNextId();
    
    /**
     * マーカー情報を取得する
     * @return マーカー情報リスト
     */
    @Select("SELECT"
            + " id"
            + ", lon"
            + ", lat"
            + ", marker_type as markerType"
            + ", marker_icon as markerIcon"
            + ", marker_comment as markerComment"
            + ", user_id as userId"
            + " FROM geko.tm_point"
            + " WHERE type = '2'"
            + " AND invalid = FALSE")
    public List<GeometryDomain> getMarkers();
    
    /**
     * 要素(マーカー、EXBeacon等)を登録する
     * @param point 要素情報 
     * @return 登録件数
     */
    @Insert("INSERT INTO geko.tm_point"
            + "(id, type, lon, lat, marker_type, marker_icon, marker_comment,"
            + " invalid, user_id, last_modified) VALUES"
            + "(#{id}, #{type}, #{lon}, #{lat}, #{markerType}, #{markerIcon}, #{markerComment},"
            + "FALSE, #{userId}, current_timestamp)")
    public int createPoint(GeometryDomain point);

    /**
     * マーカーを更新する
     * @param marker マーカー情報
     * @return 更新件数
     */
    @Update("UPDATE geko.tm_point SET"
            + " lon = #{lon}"
            + ", lat = #{lat}"
            + ", marker_type = #{markerType}"
            + ", marker_icon = #{markerIcon}"
            + ", marker_comment = #{markerComment}"
            + ", last_modified = current_timestamp"
            + " WHERE id = #{id}"
            + " AND type = '2'"
            + " AND invalid = FALSE"
            + " AND user_id = #{userId}")
    public int updateMarker(GeometryDomain marker);

    /**
     * 要素(マーカー、EXBeacon等)を論理削除する
     * @param point 要素情報
     * @return 削除件数
     */
    @Update("UPDATE geko.tm_point SET"
            + " invalid = TRUE"
            + ", last_modified = current_timestamp"
            + " WHERE id = #{id}"
            + " AND invalid = FALSE"
            + " AND user_id = #{userId}")
    public int deletePoint(GeometryDomain point);

}
