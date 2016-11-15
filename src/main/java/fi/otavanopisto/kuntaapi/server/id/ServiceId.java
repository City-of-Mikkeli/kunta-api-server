package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing service id
 * 
 * @author Antti Leppä
 */
public class ServiceId extends BaseId {

  /**
   * Zero-argument constructor for service id
   */
  public ServiceId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public ServiceId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.SERVICE;
  }
  
  @Override
  protected int getHashInitial() {
    return 133;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 145;
  }
  
}
