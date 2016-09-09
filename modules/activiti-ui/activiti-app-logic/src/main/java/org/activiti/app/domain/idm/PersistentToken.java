/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.app.domain.idm;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Persistent tokens are used by Spring Security to automatically log in users.
 *
 * @see com.mycompany.myapp.security.CustomPersistentRememberMeServices
 */
@Entity
@Table(name = "ACT_IDM_PERSISTENT_TOKEN")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersistentToken implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("d MMMM yyyy");

  @Id
  private String series;

  @JsonIgnore
  @NotNull
  @Column(name = "token_value")
  private String tokenValue;

  @JsonIgnore
  @Column(name = "token_date")
  @Temporal(TemporalType.TIMESTAMP)
  private Date tokenDate;

  // an IPV6 address max length is 39 characters
  @Size(min = 0, max = 39)
  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "user_id")
  private String user;

  public String getSeries() {
    return series;
  }

  public void setSeries(String series) {
    this.series = series;
  }

  public String getTokenValue() {
    return tokenValue;
  }

  public void setTokenValue(String tokenValue) {
    this.tokenValue = tokenValue;
  }

  public Date getTokenDate() {
    return tokenDate;
  }

  public void setTokenDate(Date tokenDate) {
    this.tokenDate = tokenDate;
  }

  @JsonGetter
  public String getFormattedTokenDate() {
    return dateTimeFormatter.print(new LocalDate(this.tokenDate));
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    if (userAgent.length() >= 255) {
      this.userAgent = userAgent.substring(0, 254);
    } else {
      this.userAgent = userAgent;
    }
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PersistentToken that = (PersistentToken) o;

    if (!series.equals(that.series)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return series.hashCode();
  }

  @Override
  public String toString() {
    return "PersistentToken{" + "series='" + series + '\'' + ", tokenValue='" + tokenValue + '\'' + ", tokenDate=" + tokenDate + ", ipAddress='" + ipAddress + '\'' + ", userAgent='" + userAgent + '\''
        + "}";
  }
}