package com.denso.geko.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.denso.geko.domain.UserDomain;
import com.denso.geko.repository.UserRepository;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    /**
     * ユーザが表示可能な児童の情報を取得する
     * @param userId ユーザID
     * @return 児童情報リスト
     */
    public List<UserDomain> searchAuthList(String userId) {
        return userRepository.getAuth(userId);
    }
    
    
    /**
     * 児童のユーザ情報(BeaconTX、センサーのID)を取得する
     * @param cuserId 児童のユーザID
     * @return 児童のユーザ情報
     */
    public UserDomain searchUser(String cuserId) {
        return userRepository.find(cuserId);
    }
    
    /**
     * ユーザが指定した児童の参照権限を保持するかチェックする。
     * @param userId ユーザID
     * @param cuserId チェック対象の児童のユーザID
     * @return チェック結果(true:権限あり、false:権限なし)
     */
    public boolean chkAuth(String userId, String cuserId) {
        boolean result = false;
        if (userRepository.exist(userId, cuserId) > 0) {
            result = true;
        }
        return result;
    }

}
