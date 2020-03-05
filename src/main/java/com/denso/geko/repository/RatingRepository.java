package com.denso.geko.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.denso.geko.domain.RatingDomain;

@Repository
@Mapper
public interface RatingRepository {
    
    /**
     * 指定した児童、期間の評価情報を取得する
     * @param domain 評価情報の検索条件
     * @return 評価情報リスト
     */
    @Select("SELECT "
            + "cuser_id as cuserId,"
            + "TO_CHAR(a.rating_date, 'YYYY/MM/DD') as ratingDate,"
            + "a.rating,"
            + "a.shortcut as shortCutCnt,"
            + "a.detour as detourCnt,"
            + "a.wrongway as wrongWayCnt,"
            + "a.elapsedtime as elapsedTime,"
            + "count(b.*) as markerCnt"
            + " FROM geko.td_rating a"
            + " LEFT OUTER JOIN geko.tm_point b"
            + " ON b.last_modified >= a.rating_date"
            + " AND b.last_modified < a.rating_date + 1"
            + " AND b.user_id = #{userId}"
            + " AND b.type = '2'"
            + " AND b.invalid = FALSE"
            + " WHERE a.cuser_id = #{cuserId}"
            + " AND a.rating_date BETWEEN TO_DATE(#{ratingDate}, 'YYYY/MM/DD') AND TO_DATE(#{ratingDate}, 'YYYY/MM/DD') + 6"
            + " GROUP BY"
            + " a.cuser_id,"
            + " a.rating_date,"
            + " a.rating,"
            + " a.shortcut,"
            + " a.detour,"
            + " a.wrongway,"
            + " a.elapsedtime"
            + " ORDER BY a.rating_date")
    public List<RatingDomain> search(RatingDomain domain);
    
    /**
     * 児童の評価情報を登録する
     * @param domain 登録する評価情報
     * @return 登録件数
     */
    @Insert("INSERT INTO geko.td_rating (cuser_id, rating_date, rating, user_id, last_modified) VALUES "
            + "(#{cuserId}, TO_DATE(#{ratingDate}, 'YYYY/MM/DD'), #{rating}, #{userId}, current_timestamp)")
    public int insertRatingOnly(RatingDomain domain);
    
    /**
     * 児童の評価情報を更新する(評価値のみ)
     * @param domain 更新する評価情報
     * @return 更新件数
     */
    @Update("UPDATE geko.td_rating SET"
            + " rating = #{rating}"
            + ", user_id = #{userId}"
            + ", last_modified = current_timestamp"
            + " WHERE cuser_id = #{cuserId}"
            + " AND rating_date = TO_DATE(#{ratingDate}, 'YYYY/MM/DD')")
    public int updateRatingOnly(RatingDomain domain);
    
    /**
     * 特定の児童、日付の評価情報を取得する
     * @param domain 評価情報の検索条件
     * @return 評価情報
     */
    @Select("SELECT "
            + "cuser_id as cuserId,"
            + "rating_date as ratingDate,"
            + "rating,"
            + "shortcut as shortCutCnt,"
            + "detour as detourCnt,"
            + "wrongway as wrongWayCnt,"
            + "elapsedtime as elapsedTime"
            + " FROM geko.td_rating"
            + " WHERE cuser_id = #{cuserId} AND rating_date = TO_DATE(#{ratingDate}, 'YYYY/MM/DD')")
    public RatingDomain find(RatingDomain domain);
    
    /**
     * 児童の評価情報を登録する(評価値以外も登録)
     * @param domain 評価情報
     * @return 登録件数
     */
    @Insert("INSERT INTO geko.td_rating "
            + "(cuser_id, rating_date, rating,"
            + " shortcut, detour, wrongway, elapsedtime,"
            + " user_id, last_modified) VALUES "
            + "(#{cuserId}, TO_DATE(#{ratingDate}, 'YYYY/MM/DD'), #{rating},"
            + " #{shortCutCnt}, #{detourCnt}, #{wrongWayCnt}, #{elapsedTime},"
            + " #{userId}, current_timestamp)")
    public int insert(RatingDomain domain);
    
    /**
     * 児童の評価情報を更新する(評価値以外も更新)
     * @param domain 評価情報
     * @return 更新件数
     */
    @Update("UPDATE geko.td_rating SET"
            + " rating = #{rating}"
            + ", shortcut = #{shortCutCnt}"
            + ", detour = #{detourCnt}"
            + ", wrongway = #{wrongWayCnt}"
            + ", elapsedtime = #{elapsedTime}"
            + ", user_id = #{userId}"
            + ", last_modified = current_timestamp"
            + " WHERE cuser_id = #{cuserId}"
            + " AND rating_date = TO_DATE(#{ratingDate}, 'YYYY/MM/DD')")
    public int update(RatingDomain domain);
}
