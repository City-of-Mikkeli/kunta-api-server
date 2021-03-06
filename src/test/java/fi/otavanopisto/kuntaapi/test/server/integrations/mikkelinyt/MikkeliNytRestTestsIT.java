package fi.otavanopisto.kuntaapi.test.server.integrations.mikkelinyt;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.Is.is;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt.MikkeliNytConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;
import fi.otavanopisto.kuntaapi.test.AbstractMocker;
import fi.otavanopisto.mikkelinyt.model.Event;
import fi.otavanopisto.mikkelinyt.model.EventsResponse;

/**
 * Tests for MikkeliNyt integration
 * 
 * @author Antti Leppä
 */
public class MikkeliNytRestTestsIT extends AbstractIntegrationTest {
  
  private static final String IMAGES_BASE_PATH = "/uploads/testevents";
  private static final String ORGANIZATION_SETTING_APIKEY = "test-api-key";
  private static final String ORGANIZATION_SETTING_LOCATION = "test";
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Europe/Helsinki");
  
  private static final String TEST_EVENT_ATTACHMENT_TYPE = "image/jpeg";
  private static final String TEST_EVENT_ATTACHMENT_SCALED_TYPE = "image/png";
  private static final String TEST_EVENT_URL_TEMPLATE = "%s/event-url-address";
  private static final String TEST_EVENT_ZIP = "12345";
  private static final String TEST_EVENT_ADDRESS = "Testroad 3";
  private static final String TEST_EVENT_PLACE = "Testing Ltd";
  private static final String TEST_EVENT_DESC = "My test<br/>event";
  private static final String TEST_EVENT_NAME = "Test Event";

  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  private MikkeliNytMocker mocker;

  @Before
  public void beforeTest() throws InterruptedException {
    String baseUrl = getWireMockBasePath();
    String imagesBasePath = IMAGES_BASE_PATH;
    
    createSystemSettings();
    
    getPtvMocker()
      .mockOrganizations("0de268cf-1ea1-4719-8a6e-1150933b6b9e")
      .startMock();
    
    waitApiListCount("/organizations", 1);
    
    String organizationId = getOrganizationId(0);
        
    createOrganizationSettings(organizationId, baseUrl, imagesBasePath);
    
    mocker = new MikkeliNytMocker();
    mockEvent(mocker, baseUrl, imagesBasePath);
    mocker.startMock();

    waitApiListCount(String.format("/organizations/%s/events", organizationId), 1);
  }

  @After
  public void afterClass() {
    deleteOrganizationSettings(getOrganizationId(0));
    cleanMock(mocker);
    getPtvMocker().endMock();
    deleteSystemSettings();
  }
  
  /**
   * Tests events endpoint
   */
  @Test
  public void testEvents() {
    String organizationId = getOrganizationId(0);
    String eventId = getEventId(organizationId, 0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/events", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(eventId))
      .body("originalUrl[0]", is(String.format(TEST_EVENT_URL_TEMPLATE, getWireMockBasePath())))
      .body("name[0]", is(TEST_EVENT_NAME))
      .body("description[0]", is(TEST_EVENT_DESC))
      .body("start[0]", sameInstant(getInstant(2020, 5, 6, 17, 30, TIMEZONE_ID)))
      .body("end[0]", sameInstant(getInstant(2020, 5, 6, 19, 00, TIMEZONE_ID)))
      .body("city[0]", is(ORGANIZATION_SETTING_LOCATION))
      .body("place[0]", is(TEST_EVENT_PLACE))
      .body("address[0]", is(TEST_EVENT_ADDRESS))
      .body("zip[0]", is(TEST_EVENT_ZIP));
  }
  
  /**
   * Tests single event
   */
  @Test
  public void testEvent() {
    String organizationId = getOrganizationId(0);
    String baseUrl = getWireMockBasePath();
    String eventId = getEventId(organizationId, 0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/events/{EVENTID}", organizationId, eventId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(eventId))
      .body("originalUrl", is(String.format(TEST_EVENT_URL_TEMPLATE, baseUrl)))
      .body("name", is(TEST_EVENT_NAME))
      .body("description", is(TEST_EVENT_DESC))
      .body("start", sameInstant(getInstant(2020, 5, 6, 17, 30, TIMEZONE_ID)))
      .body("end", sameInstant(getInstant(2020, 5, 6, 19, 00, TIMEZONE_ID)))
      .body("city", is(ORGANIZATION_SETTING_LOCATION))
      .body("place", is(TEST_EVENT_PLACE))
      .body("address", is(TEST_EVENT_ADDRESS))
      .body("zip", is(TEST_EVENT_ZIP));
     
  }
  
  /**
   * Tests event image metadata retrieval
   */
  @Test
  public void testImage() {
    String organizationId = getOrganizationId(0);
    String eventId = getEventId(organizationId, 0);
    String imageId = getEventImageId(organizationId, eventId, 0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/events/{EVENTID}/images/{IMAGEID}", organizationId, eventId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(imageId))
      .body("contentType", is(TEST_EVENT_ATTACHMENT_TYPE));
  }

  /**
   * Tests listing of event images
   */
  @Test
  public void testImages() {
    String organizationId = getOrganizationId(0);
    String eventId = getEventId(organizationId, 0);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/events/{EVENTID}/images", organizationId, eventId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("contentType[0]", is(TEST_EVENT_ATTACHMENT_TYPE));
  }
  
  /**
   * Tests retrieval of event image data
   */
  @Test
  public void testImageData() {
    String organizationId = getOrganizationId(0);
    String eventId = getEventId(organizationId, 0);
    String imageId = getEventImageId(organizationId, eventId, 0);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/events/{EVENTID}/images/{IMAGEID}/data", organizationId, eventId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Type", TEST_EVENT_ATTACHMENT_TYPE);
  }
  
  /**
   * Tests retrieval of event scaled image data
   */
  @Test
  public void testImageDataScaled() {
    String organizationId = getOrganizationId(0);
    String eventId = getEventId(organizationId, 0);
    String imageId = getEventImageId(organizationId, eventId, 0);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{ORGANIZATIONID}/events/{EVENTID}/images/{IMAGEID}/data?size=100", organizationId, eventId, imageId)
      .then()
      .assertThat()
      .statusCode(200)
      .header("Content-Type", TEST_EVENT_ATTACHMENT_SCALED_TYPE);
  }

  private Event mockEvent(MikkeliNytMocker mocker, String baseUrl, String imagesBasePath) {
    String eventId = "mn-event-id";
    String attachmentId = "mn-attachment-id.jpg";
    String eventUrl = String.format(TEST_EVENT_URL_TEMPLATE, baseUrl);
    String eventName = TEST_EVENT_NAME;
    String eventDescription = TEST_EVENT_DESC;
    String eventCity = ORGANIZATION_SETTING_LOCATION;
    String eventAddres = TEST_EVENT_ADDRESS;
    String eventZip = TEST_EVENT_ZIP;
    String eventPlace = TEST_EVENT_PLACE;
    String eventThumbPath = String.format("%s/480/%s", imagesBasePath, attachmentId);
    String eventImagePath = String.format("%s/1000/%s", imagesBasePath, attachmentId);
    String eventThumb = String.format("%s%s", baseUrl, eventThumbPath);
    String eventImage = String.format("%s%s", baseUrl, eventImagePath);
    
    OffsetDateTime start = getOffsetDateTime(2020, 5, 6, 17, 30, TIMEZONE_ID);
    OffsetDateTime end = getOffsetDateTime(2020, 5, 6, 19, 00, TIMEZONE_ID);

    mocker.mockGetBinary(eventImagePath, TEST_EVENT_ATTACHMENT_TYPE, "test-image-1000.jpg");
    
    return mocker.mockEvent(eventId, eventUrl, eventName, eventDescription, eventCity, eventAddres, eventZip, eventPlace, eventImage, eventThumb, start, end);
  }

  private void cleanMock(MikkeliNytMocker mocker) {
    mocker.endMock();
  }
  
  private void createSystemSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), BASE_URL));
  }

  private void createOrganizationSettings(String organizationId, String baseUrl, String imagesBasePath) {
    insertOrganizationSetting(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_APIKEY, ORGANIZATION_SETTING_APIKEY);
    insertOrganizationSetting(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_LOCATION, ORGANIZATION_SETTING_LOCATION);
    insertOrganizationSetting(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_BASEURL, baseUrl);
    insertOrganizationSetting(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_IMAGEBASEURL, String.format("%s/%s/1000/", baseUrl, imagesBasePath));
  }
  
  private void deleteOrganizationSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_APIKEY);
    deleteOrganizationSetting(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_LOCATION);
    deleteOrganizationSetting(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_BASEURL);
    deleteOrganizationSetting(organizationId, MikkeliNytConsts.ORGANIZATION_SETTING_IMAGEBASEURL);
  }
  
  private void deleteSystemSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
  
  private class MikkeliNytMocker extends AbstractMocker {
    
    private List<Event> events;
    
    public MikkeliNytMocker() {
      this.events = new ArrayList<>();
    }
    
    /**
     * Mocks a MikkeliNyt event
     * 
     * @param id MikkeliNyt id
     * @param url MikkeliNyt URL
     * @param name MikkeliNyt name
     * @param description MikkeliNyt description
     * @param city MikkeliNyt city
     * @param address MikkeliNyt address 
     * @param zip MikkeliNyt ZIP
     * @param place MikkeliNyt place
     * @param image MikkeliNyt image
     * @param thumb MikkeliNyt thumb
     * @param start MikkeliNyt start
     * @param end MikkeliNyt end
     * @return
     */
    @SuppressWarnings ("squid:S00107")
    public Event mockEvent(String id, String url, String name, String description, String city, String address, String zip, String place, String image, String thumb, OffsetDateTime start, OffsetDateTime end) {
      String location = String.format("%s, %s", formatAddress(address), formatZip(zip));
      
      Event event = new Event();
      event.setAddress(formatAddress(address));
      event.setCity(formatCity(city));
      event.setDescription(description);
      event.setEnd(formatDateTime(end));
      event.setId(id);
      event.setImage(image);
      event.setLocation(location);
      event.setName(name);
      event.setNiceDatetime(formatNiceDateRange(start, end));
      event.setPlace(formatPlace(place));
      event.setStart(formatDateTime(start));
      event.setThumb(thumb);
      event.setUrl(url);
      event.setZip(formatZip(zip));

      this.events.add(event);
      
      return event;
    }
    
    private String formatAddress(String address) {
      return String.format("<span class=\"Address\">%s</span>", address);
    }
    
    private String formatZip(String zip) {
      return String.format("<span class=\"Zip\">%s</span>", zip);
    }
    
    private String formatCity(String zip) {
      return String.format("<span class=\"City\">%s</span>", zip);
    }
    
    private String formatPlace(String place) {
      return String.format("<span class=\"Place\">%s</span>", place);
    }
    
    private String formatDateTime(OffsetDateTime localDateTime) {
      DateTimeFormatter formatter = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral(' ')
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .toFormatter();
      
      return localDateTime.format(formatter);
    }
    
    private String formatNiceDateRange(OffsetDateTime from, OffsetDateTime to) {
      DateTimeFormatter startDateFormat = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.DAY_OF_MONTH)
        .appendLiteral('.')
        .appendValue(ChronoField.MONTH_OF_YEAR)
        .toFormatter();
      
      DateTimeFormatter endDateFormat = new DateTimeFormatterBuilder()
          .appendValue(ChronoField.DAY_OF_MONTH)
          .appendLiteral('.')
          .appendValue(ChronoField.MONTH_OF_YEAR)
          .appendLiteral('.')
          .appendValue(ChronoField.YEAR)
          .toFormatter();
      
      DateTimeFormatter timeFormat = new DateTimeFormatterBuilder()
          .appendValue(ChronoField.HOUR_OF_DAY)
          .appendLiteral(':')
          .appendValue(ChronoField.MINUTE_OF_HOUR)
          .toFormatter();
      
      return String.format("%s - %s klo %s - %s", 
          from.format(startDateFormat), 
          to.format(endDateFormat), 
          from.format(timeFormat), 
          to.format(timeFormat));
    }
    
    @Override
    public void startMock() {
      EventsResponse eventsResponse = new EventsResponse();
      eventsResponse.setData(events);
      eventsResponse.setMessage(String.format("Löytyi %d tapahtumaa", events.size()));
      eventsResponse.setStatus(1);
      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("showall", "1");
      queryParams.put("apiKey", ORGANIZATION_SETTING_APIKEY);
      queryParams.put("location", ORGANIZATION_SETTING_LOCATION);
      mockGetJSON("/json.php", eventsResponse, queryParams);
      super.startMock();
    }
  }
  
  
}
