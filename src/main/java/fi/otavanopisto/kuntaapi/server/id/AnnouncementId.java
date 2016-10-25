package fi.otavanopisto.kuntaapi.server.id;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing announcement id
 * 
 * @author Antti Leppä
 */
public class AnnouncementId extends Id {
  
  /**
   * Zero-argument constructor for announcement id
   */
  public AnnouncementId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public AnnouncementId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public AnnouncementId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.ANNOUNCEMENT;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AnnouncementId) {
      AnnouncementId another = (AnnouncementId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(235, 247)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
