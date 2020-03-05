package com.denso.geko.service;

//import java.net.InetSocketAddress;
//import java.net.Proxy;
//import java.net.Proxy.Type;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.List;
//import java.util.stream.Collectors;
import java.util.Calendar;

//import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.MediaType;
//import org.springframework.http.RequestEntity;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;

//import com.denso.geko.Constants.CommonConstants;
//import com.denso.geko.domain.GeometryDomain;
import com.denso.geko.domain.LocationDomain;
//import com.denso.geko.domain.UserDomain;
//import com.denso.geko.repository.GeometryRepository;
//import com.denso.geko.repository.UserRepository;
import com.denso.geko.repository.LocationRepository;

@Service
@Transactional
public class LocationService {
    
//    @Value("${httpclient.proxy.hostname}")
//    private String proxyHostname;

//    @Value("${httpclient.proxy.port}")
//    private int proxyPort;

//    @Autowired
//    private UserRepository userRepository;

//    @Autowired
//    private GeometryRepository geometryRepository;

    @Autowired
    private LocationRepository locationRepository;
    
    /**
     * 児童の現在地を取得する
     * @param userId 児童のユーザID
     * @return 現在地情報
     */
    public LocationDomain getLocation(String userId) {
        
//        List<LocationDomain> locations = null;
//        LocationDomain location = null;
        
//        // 最新の位置情報を取得する
//        try {
//            URI uri = new URI(CommonConstants.EXCLOUD_API_URI);
//            
//            RequestEntity<Void> request = RequestEntity
//                    .get(uri)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .header(CommonConstants.HEADER_STRING, CommonConstants.EXCLOUD_APIKEY)
//                    .build();
//            RestTemplate restTemplate;
//            if (Strings.isEmpty(proxyHostname)) {
//                restTemplate = new RestTemplate();
//            } else {
//                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//                factory.setProxy(new Proxy(Type.HTTP, new InetSocketAddress(proxyHostname, proxyPort)));
//                restTemplate = new RestTemplate(factory);
//            }
//            ResponseEntity<List<LocationDomain>> response =
//                    restTemplate.exchange(request, new ParameterizedTypeReference<List<LocationDomain>>(){});
//            locations = response.getBody();
//            
//        } catch (URISyntaxException e) {
//            // error
//        }        
        
        // 現在日時の5分前をUNIX TIMEで取得する
        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.MINUTE, -5);
        cal.add(Calendar.HOUR, -12);
        long fromtime = cal.getTimeInMillis();

//        // 児童のBeaconTXのIDの位置情報を取得する
//        UserDomain user = userRepository.find(userId);
//        locations = locations.stream().filter(l -> l.getBtxId() == user.getBtxId()).collect(Collectors.toList());

//        // 児童の現在位置の座標を取得する
//        if (locations.size() > 0) {
//            location = locations.get(0);
//            GeometryDomain point = geometryRepository.getPoint(location.getBeaconId());
//            if (point != null) {
//                location.setLat(point.getLat());
//                location.setLon(point.getLon());
//            }
//        }
        
        // データベースから現在地を取得するためのメソッドを呼び出す
        LocationDomain location = locationRepository.getLocation(userId, fromtime);

        return location;
    }

}
