package com.denso.geko.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.prefs.CsvPreference;

import com.denso.geko.Constants.CommonConstants;
import com.denso.geko.domain.LocationDomain;
import com.denso.geko.domain.SensorDomain;
import com.denso.geko.service.RouteService;
import com.denso.geko.service.SensorService;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.mygreen.supercsv.io.LazyCsvAnnotationBeanReader;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Controller
@Secured("IS_AUTHENTICATED_REMEMBERED")
public class WebAppController {

    @Autowired
    private SensorService sensorService;
    
    @Autowired
    private RouteService routeService;
    
    /**
     * ログイン画面用のコントローラーメソッド
     * @return　"login"
     */
    @RequestMapping(value = "/login")
    public String login() {
        return "login";
    }

    /**
     * 地図表示画面用のコントローラーメソッド
     * @param request リクエスト
     * @param response レスポンス
     * @param model モデル(トークン情報を設定)
     * @return "map"(map.htmlに遷移)
     */
    @RequestMapping(value = "/map")
    public String map(
            WebRequest request,
            HttpServletResponse response,
            Model model
            ) {
        // JWTのキーを生成
        String token = Jwts.builder()
                .setSubject(request.getRemoteUser())
                .setExpiration(new Date(System.currentTimeMillis() + CommonConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, CommonConstants.SECRET.getBytes())
                .compact();
        model.addAttribute("token", token);
        return "map";
    }
    
    /**
     * 管理者画面用のコントローラメソッド
     * @param request リクエスト
     * @param response レスポンス
     * @param model モデル(トークン情報を設定)
     * @return "admin"(admin.htmlに遷移)
     */
    @RequestMapping(value = "/admin", method=RequestMethod.GET)
    public String admin(
            WebRequest request,
            HttpServletResponse response,
            Model model
            ) {
        // JWTのキーを生成
        String token = Jwts.builder()
                .setSubject(request.getRemoteUser())
                .setExpiration(new Date(System.currentTimeMillis() + CommonConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, CommonConstants.SECRET.getBytes())
                .compact();
        model.addAttribute("token", token);
        return "admin";
    }
    
    /**
     * センサーデータアップロード用のコントローラメソッド
     * @param request リクエスト
     * @param file センサーデータのCSVファイル
     * @param model モデル(トークン情報を設定)
     * @return "admin"(admin.htmlに遷移)
     */
    @RequestMapping(value = "/admin/sensor", method=RequestMethod.POST)
    public String updateSensor(
            WebRequest request,
            @RequestParam("sensor_file") MultipartFile file,
            Model model) {
        List<String> errors = new ArrayList<String>();
        String[] data = new String[6];
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));){
            String line;
            line = br.readLine(); // 1行目
            if (!"Version,DeviceSerialNo,StartDatetime,Interval[ms],Record Count,DeviceName".equals(line)) {
                errors.add("指定したファイルのフォーマットが不正です。");
            }
            if (errors.size() == 0) {
                line = br.readLine(); // 2行目
                data = line.split(",", 0);
                if (Strings.isEmpty(data[1])) {
                    errors.add("DeviceSerialNo が指定されていません。");
                }
                if (Strings.isEmpty(data[2])) {
                    errors.add("StartDatetime が指定されていません。");
                }
                if (Strings.isEmpty(data[4])) {
                    errors.add("Record Count が指定されていません。");
                }
            }
            
            if (errors.size() == 0) {
                String sensorId = data[1];
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date startDatetime = sdf.parse(data[2]);
                int recCnt = Integer.parseInt(data[4]);
                
                try (LazyCsvAnnotationBeanReader<SensorDomain> csvReader = new LazyCsvAnnotationBeanReader<>(
                        SensorDomain.class, br, CsvPreference.STANDARD_PREFERENCE);) {
                    // 全件読み込み　例外発生しても処理継続
                    List<SensorDomain> sensors = csvReader.readAll(true);
                    errors.addAll(csvReader.getErrorMessages());
                    if (sensors.size() == 0) {
                        errors.add("登録するデータが存在しません");
                    }
                    
                    if (errors.size() == 0) {
                        // センサーデータを加工
                        sensors = sensors.stream().map(s -> {
                            s.setSensorId(sensorId);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(startDatetime);
                            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(s.getTime().substring(0, 1)));
                            cal.add(Calendar.MINUTE, Integer.parseInt(s.getTime().substring(2, 4)));
                            cal.add(Calendar.SECOND, Integer.parseInt(s.getTime().substring(5, 7)));
                            cal.add(Calendar.MILLISECOND, Integer.parseInt(s.getTime().substring(8)) * 10);
                            s.setRecvTimestamp(cal.getTimeInMillis());
                            return s;})
                            .collect(Collectors.toList());
                        int cnt = sensorService.insert(sensors);
                        model.addAttribute("msg", recCnt + "件中 " +  cnt + "件登録が完了しました。");
                    }
                } catch (SuperCsvException e) {
                    // その他のCSV読み込みエラー
                    errors.add(e.getMessage());
                } catch (IllegalStateException e) {
                    errors.add(e.getMessage());
                } catch (IOException e) {
                    // レコードの読み込みに失敗
                    errors.add(e.getMessage());
                }
            }
        } catch (ParseException e) {
            errors.add(e.getMessage());
        } catch (IOException e) {
            errors.add(e.getMessage());
        }
        if (errors.size() > 0) {
            model.addAttribute("error", errors);
        }
        // JWTのキーを生成
        String token = Jwts.builder()
                .setSubject(request.getRemoteUser())
                .setExpiration(new Date(System.currentTimeMillis() + CommonConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, CommonConstants.SECRET.getBytes())
                .compact();
        model.addAttribute("token", token);
        return "admin";
    }
    
    /**
     * 測位情報、センサー情報ダウンロード用のコントローラメソッド
     * @param response レスポンス
     * @param auth 権限情報
     * @param date 日付
     * @param btxId タグID
     * @param type ダウンロードするデータ(1:測位情報、2:センサー情報)
     * 
     * @return CSVファイル
     * @throws IOException 例外
     */
    @RequestMapping(value = "/admin/download", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8")
    @ResponseBody
    public Object download(
            HttpServletResponse response,
            Authentication auth,
            @RequestParam(name = "date", required = true) String date,
            @RequestParam(name = "btxId") int btxId,
            @RequestParam(name = "type") String type) throws IOException {

        CsvMapper mapper = new CsvMapper();

        String filename = date.replace("-", "") + "_" + btxId + ".csv";
        Object csv = null;
        if ("1".equals(type)) {
            // 測位情報を取得する
            filename = "location_" + filename;
            List<LocationDomain>list = routeService.download(btxId, date.replaceAll("-", ""));
            CsvSchema schema = mapper.schemaFor(LocationDomain.class).withHeader();
            csv = mapper.writer(schema).writeValueAsString(list);
        } else if ("2".equals(type)) {
            // センサー情報を取得する
            filename = "sensor_" + filename;
            List<SensorDomain>list = sensorService.download(btxId, date.replaceAll("-", ""));
            CsvSchema schema = mapper.schemaFor(SensorDomain.class).withHeader();
            csv = mapper.writer(schema).writeValueAsString(list);
        }
        
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        return csv;
    }
}
