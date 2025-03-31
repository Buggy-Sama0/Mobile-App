package hk.edu.hkmu.myapplication.model;

public class StopEta {


    private String routeId;      // 路線編號
    private String direction;    // 方向
    private String serviceType;  // 服務類型
    private String etaTime;      // 預計到達時間 (ISO8601 format)
    private String remarkTC;     // 備註(中文)
    private String remarkEN;     // 備註(英文)
    private String destEN;


    public StopEta(String routeId, String direction, String serviceType,
                    String etaTime, String remarkTC, String remarkEN, String destEn) {
        this.routeId = routeId;
        this.direction = direction;
        this.serviceType = serviceType;
        this.etaTime = etaTime;
        this.remarkTC = remarkTC;
        this.remarkEN = remarkEN;
        this.destEN=destEn;

    }

    public String getRouteId() {
        return routeId;
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

    public String getDestEN() {
        return destEN;
    }


}
