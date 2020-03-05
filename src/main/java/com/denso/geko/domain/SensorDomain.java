package com.denso.geko.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.mygreen.supercsv.annotation.CsvBean;
import com.github.mygreen.supercsv.annotation.CsvColumn;
import com.github.mygreen.supercsv.annotation.CsvPartial;
import com.github.mygreen.supercsv.annotation.constraint.CsvPattern;
import com.github.mygreen.supercsv.annotation.constraint.CsvRequire;
import com.github.mygreen.supercsv.annotation.constraint.CsvUnique;

@CsvBean(header=true, validateHeader=true)
@CsvPartial(columnSize=12, headers= {
        @CsvPartial.Header(number=1, label="line"),
        @CsvPartial.Header(number=11, label="MagnetCount"),
        @CsvPartial.Header(number=12, label="MagnetSwitch")
})
@JsonPropertyOrder({"sensorId", "recvTimestamp", "acclX", "acclY", "acclZ", "anglX", "anglY", "anglZ", "temperature", "pressure", "time"})
public class SensorDomain implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * sensor id
     */
    private String sensorId;
    
    /**
     * 検知日時(JST)
     */
    @CsvColumn(label="time")
    @CsvRequire
    @CsvUnique(order=2)
    @CsvPattern(regex="(\\d{1,2}):(\\d{2}):(\\d{2}).(\\d{2})", order=3)
    private String time;
    
    /**
     * 検知日時(Unix time)
     */
    private long recvTimestamp;
    
    /**
     * 加速度(x軸)
     */
    @CsvColumn(label="Acceleration(X)[g]")
    @CsvRequire
    @CsvPattern(regex="[-]?\\d+(?:\\.\\d+)?", order=2)
    private String acclX;
    
    /**
     * 加速度(y軸)
     */
    @CsvColumn(label="Acceleration(Y)[g]")
    @CsvRequire
    @CsvPattern(regex="[-]?\\d+(?:\\.\\d+)?", order=2)
    private String acclY;
    
    /**
     * 加速度(z軸)
     */
    @CsvColumn(label="Acceleration(Z)[g]")
    @CsvRequire
    @CsvPattern(regex="[-]?\\d+(?:\\.\\d+)?", order=2)
    private String acclZ;
    
    /**
     * 角速度(x軸)
     */
    @CsvColumn(label="AngularRate(X)[dps]")
    @CsvRequire
    @CsvPattern(regex="[-]?\\d+(?:\\.\\d+)?", order=2)
    private String anglX;
    
    /**
     * 角速度(y軸)
     */
    @CsvColumn(label="AngularRate(Y)[dps]")
    @CsvRequire
    @CsvPattern(regex="[-]?\\d+(?:\\.\\d+)?", order=2)
    private String anglY;
    
    /**
     * 角速度(z軸)
     */
    @CsvColumn(label="AngularRate(Z)[dps]")
    @CsvRequire
    @CsvPattern(regex="[-]?\\d+(?:\\.\\d+)?", order=2)
    private String anglZ;
    
    /**
     * 気温
     */
    @CsvColumn(label="Temperature[degree]")
    @CsvPattern(regex="[-]?\\d+(?:\\.\\d+)?", order=2)
    private String temperature;
    
    /**
     * 気圧
     */
    @CsvColumn(label="Pressure[hPa]")
    @CsvPattern(regex="[-]?\\d+(?:\\.\\d+)?", order=2)
    private String pressure;

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getRecvTimestamp() {
        return recvTimestamp;
    }

    public void setRecvTimestamp(long recvTimestamp) {
        this.recvTimestamp = recvTimestamp;
    }

    public String getAcclX() {
        return acclX;
    }

    public void setAcclX(String acclX) {
        this.acclX = acclX;
    }

    public String getAcclY() {
        return acclY;
    }

    public void setAcclY(String acclY) {
        this.acclY = acclY;
    }

    public String getAcclZ() {
        return acclZ;
    }

    public void setAcclZ(String acclZ) {
        this.acclZ = acclZ;
    }

    public String getAnglX() {
        return anglX;
    }

    public void setAnglX(String anglX) {
        this.anglX = anglX;
    }

    public String getAnglY() {
        return anglY;
    }

    public void setAnglY(String anglY) {
        this.anglY = anglY;
    }

    public String getAnglZ() {
        return anglZ;
    }

    public void setAnglZ(String anglZ) {
        this.anglZ = anglZ;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }
}
