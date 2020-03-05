package com.denso.geko.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.denso.geko.domain.UserDomain;

@Repository
@Mapper
public interface UserRepository {
    
    /**
     * 指定したユーザのユーザ情報を取得する
     * @param username ユーザID
     * @return ユーザ情報
     */
    @Select("SELECT "
            + "password,"
            + "note,"
            + "btx_id as btxId,"
            + "sensor_id as sensorId,"
            + "sensor2_id as sensor2Id,"
            + "role"
            + " FROM geko.tm_user"
            + " WHERE user_id = #{username}"
            + " AND invalid = false")
    public UserDomain find(String username);
    
    /**
     * ユーザが表示可能な児童の情報を取得する
     * @param userId ユーザID
     * @return 表示可能な児童のユーザ情報リスト
     */
    @Select("SELECT "
            + "a.user_id as userId,"
            + "a.note,"
            + "a.btx_id as btxId,"
            + "a.sensor_id as sensorId"
            + " FROM geko.tm_user a"
            + " INNER JOIN geko.tm_authority b"
            + " ON b.cuser_id = a.user_id"
            + " WHERE b.user_id = #{userId}"
            + " AND a.invalid = false"
            + " ORDER BY a.user_id")
    public List<UserDomain> getAuth(String userId);

    /**
     * 対象の児童の権限を保持しているかをチェックする
     * @param userId ユーザID
     * @param cuserId チェック対象の児童のユーザID
     * @return レコード件数（0件：権限なし、1件：権限あり)
     */
    @Select("SELECT count(*) FROM geko.tm_authority a"
            + " INNER JOIN geko.tm_user b"
            + " ON b.user_id = a.cuser_id"
            + " WHERE a.user_id = #{userId}"
            + " AND a.cuser_id = #{cuserId}"
            + " AND b.invalid = false")
    public int exist(@Param("userId")String userId, @Param("cuserId")String cuserId);
}
