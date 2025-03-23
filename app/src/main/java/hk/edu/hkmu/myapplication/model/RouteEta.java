package hk.edu.hkmu.myapplication.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 巴士預計到達時間模型類
 */
public class RouteEta {
    private String routeId;      // 路線編號
    private String stopId;       // 站點編號
    private String direction;    // 方向
    private String serviceType;  // 服務類型
    private String etaTime;      // 預計到達時間 (ISO8601 format)
    private String remarkTC;     // 備註(中文)
    private String remarkEN;     // 備註(英文)
    
    private long minutesRemaining = -1; // 剩餘分鐘數

    public RouteEta(String routeId, String stopId, String direction, String serviceType, 
                   String etaTime, String remarkTC, String remarkEN) {
        this.routeId = routeId;
        this.stopId = stopId;
        this.direction = direction;
        this.serviceType = serviceType;
        this.etaTime = etaTime;
        this.remarkTC = remarkTC;
        this.remarkEN = remarkEN;
        
        calculateMinutesRemaining();
    }

    public String getRouteId() {
        return routeId;
    }

    public String getStopId() {
        return stopId;
    }

    public String getDirection() {
        return direction;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getEtaTime() {
        return etaTime;
    }

    public String getRemarkTC() {
        return remarkTC;
    }

    public String getRemarkEN() {
        return remarkEN;
    }
    
    public long getMinutesRemaining() {
        return minutesRemaining;
    }
    
    // 根據語言獲取備註
    public String getRemark(boolean isEnglish) {
        return isEnglish ? remarkEN : remarkTC;
    }
    
    // 計算剩餘分鐘數
    private void calculateMinutesRemaining() {
        if (etaTime == null || etaTime.isEmpty()) {
            minutesRemaining = -1;
            return;
        }
        
        try {
            // 嘗試解析ISO 8601格式的時間
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            Date etaDate = sdf.parse(etaTime);
            Date now = new Date();
            
            // 添加調試日誌
            System.out.println("ETA計算: 路線=" + routeId + ", 站點=" + stopId + 
                    ", 原始時間=" + etaTime + 
                    ", 解析時間=" + etaDate + 
                    ", 當前時間=" + now);
            
            long diffInMillis = etaDate.getTime() - now.getTime();
            minutesRemaining = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            
            // 調試日誌
            System.out.println("ETA計算結果: 毫秒差=" + diffInMillis + ", 分鐘差=" + minutesRemaining);
            
            // 如果小於0，表示巴士已經過站，但為了顯示"即將到達"，我們將-1到0分鐘的值設為0
            if (minutesRemaining < -1) {
                minutesRemaining = -1; // 已過站
            } else if (minutesRemaining < 0) {
                minutesRemaining = 0; // 即將到達
            }
        } catch (ParseException e) {
            // 如果解析失敗，嘗試使用其他常見格式
            try {
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
                Date etaDate = sdf2.parse(etaTime);
                Date now = new Date();
                
                long diffInMillis = etaDate.getTime() - now.getTime();
                minutesRemaining = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                
                if (minutesRemaining < -1) {
                    minutesRemaining = -1;
                } else if (minutesRemaining < 0) {
                    minutesRemaining = 0;
                }
                
                System.out.println("ETA計算(第二嘗試成功): 分鐘差=" + minutesRemaining);
            } catch (ParseException e2) {
                // 所有解析嘗試都失敗，設置為-1
                System.out.println("ETA時間格式無法解析: " + etaTime + ", 錯誤: " + e.getMessage());
                minutesRemaining = -1;
            }
        }
    }
} 