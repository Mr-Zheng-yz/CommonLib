package com.baize.mqtt_lib.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;

/**
    clientId = 05_2__NSR1560220240730
    brokerUrl = "tcp://10.10.5.35:1883";
    userName = "admin";
    password = "1234";
 */
public class MqttEntity implements Parcelable {
   String clientId;
   String brokerUrl;
   String userName;
   String password;
   List<String> topics;

   public MqttEntity(String clientId, String brokerUrl, String userName, String password, List<String> topics) {
      this.clientId = clientId;
      this.brokerUrl = brokerUrl;
      this.userName = userName;
      this.password = password;
      this.topics = topics;
   }

   public MqttEntity() {}

   //region 序列化实现
   protected MqttEntity(Parcel in) {
      clientId = in.readString();
      brokerUrl = in.readString();
      userName = in.readString();
      password = in.readString();
      topics = in.createStringArrayList();
   }

   public static final Creator<MqttEntity> CREATOR = new Creator<MqttEntity>() {
      @Override
      public MqttEntity createFromParcel(Parcel in) {
         return new MqttEntity(in);
      }

      @Override
      public MqttEntity[] newArray(int size) {
         return new MqttEntity[size];
      }
   };

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(@NonNull Parcel dest, int flags) {
      dest.writeString(clientId);
      dest.writeString(brokerUrl);
      dest.writeString(userName);
      dest.writeString(password);
      dest.writeStringList(topics);
   }
   //endregion 序列化实现end


   public String getClientId() {
      return clientId;
   }

   public void setClientId(String clientId) {
      this.clientId = clientId;
   }

   public String getBrokerUrl() {
      return brokerUrl;
   }

   public void setBrokerUrl(String brokerUrl) {
      this.brokerUrl = brokerUrl;
   }

   public String getUserName() {
      return userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public List<String> getTopics() {
      return topics;
   }

   public void setTopics(List<String> topics) {
      this.topics = topics;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof MqttEntity)) return false;
      MqttEntity that = (MqttEntity) o;
      return Objects.equals(clientId, that.clientId) && Objects.equals(brokerUrl, that.brokerUrl) && Objects.equals(userName, that.userName) && Objects.equals(password, that.password) && Objects.equals(topics, that.topics);
   }

   @Override
   public int hashCode() {
      return Objects.hash(clientId, brokerUrl, userName, password, topics);
   }

   @Override
   public String toString() {
      return "MqttEntity{" +
              "clientId='" + clientId + '\'' +
              ", brokerUrl='" + brokerUrl + '\'' +
              ", userName='" + userName + '\'' +
              ", password='" + password + '\'' +
              ", topics=" + topics +
              '}';
   }
}
