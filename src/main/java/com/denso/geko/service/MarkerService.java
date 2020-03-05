package com.denso.geko.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.denso.geko.domain.GeometryDomain;
import com.denso.geko.repository.GeometryRepository;

@Service
@Transactional
public class MarkerService {
    
    @Autowired
    private GeometryRepository geometryRepository;
    
    /**
     * マーカー情報を取得する
     * @param userId ユーザID
     * @return マーカー情報リスト
     */
    public List<GeometryDomain> getMarker(String userId) {
        
        // マーカー情報を取得する
        List<GeometryDomain>markers = geometryRepository.getMarkers();
        
        // 対象ユーザが編集不可なマーカーはidを削除する
        markers.stream()
            .filter(m -> !userId.equals(m.getUserId()))
            .forEach(m -> m.setId(0));
        
        return markers;
    }
    
    /**
     * マーカー情報を登録する
     * @param marker マーカー情報
     * @return 新規登録したマーカーのID
     */
    public int create(GeometryDomain marker) {
        // 新規登録するマーカーID(マーカーIDの最大値)を取得する
        int markerId = geometryRepository.getNextId();
        marker.setId(markerId);
        
        // 新規マーカーを登録する
        marker.setType("2");
        geometryRepository.createPoint(marker);
        
        return markerId;
    }

    /**
     * マーカー情報を更新する
     * @param marker マーカー情報
     * @return 更新件数
     */
    public int update(GeometryDomain marker) {
        // マーカーを更新する
        return geometryRepository.updateMarker(marker);
    }
    
    /**
     * マーカー情報を削除する
     * @param marker マーカー情報
     * @return 削除件数
     */
    public int delete(GeometryDomain marker) {
        // マーカーを削除する
        return geometryRepository.deletePoint(marker);
    }
}
