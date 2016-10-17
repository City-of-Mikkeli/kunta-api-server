package fi.otavanopisto.kuntaapi.server.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.ElectronicChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PhoneChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PrintableFormChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceLocationChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.WebPageChannel;

/**
 * REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
@SuppressWarnings ("squid:S3306")
public class ServicesApiImpl extends ServicesApi {
  
  private static final String INVALID_SERVICE_ID = "Invalid service id %s";

  private static final String NOT_IMPLEMENTED = "Not implemented";
  
  @Inject
  private Instance<ServiceProvider> serviceProviders;
  
  @Inject
  private Instance<ServiceChannelProvider> serviceChannelProviders;
  
  @Override
  public Response createService(Service body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findService(String serviceIdParam) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    
    for (ServiceProvider serviceProvider : getServiceProviders()) {
      Service service = serviceProvider.findService(serviceId);
      if (service != null) {
        return Response.ok(service)
          .build();
      }
    }
    
    return Response
        .status(Status.NOT_FOUND)
        .build();
  }
  
  @Override
  public Response listServices(Long firstResult, Long maxResults) {
    List<Service> result = new ArrayList<>();
    
    for (ServiceProvider serviceProvider : getServiceProviders()) {
      result.addAll(serviceProvider.listServices(null, null));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }
  
  @Override
  public Response updateService(String serviceId, Service body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response createServiceElectronicChannel(String serviceId, ElectronicChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServicePhoneChannel(String serviceId, PhoneChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServicePrintableFormChannel(String serviceId, PrintableFormChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServiceServiceLocationChannel(String serviceId, ServiceLocationChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServiceWebPageChannel(String serviceId, WebPageChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServiceElectronicChannel(String serviceId, String electronicChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServicePhoneChannel(String serviceId, String phoneChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServicePrintableFormChannel(String serviceId, String printableFormChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServiceServiceLocationChannel(String serviceId, String serviceLocationChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServiceWebPageChannel(String serviceId, String webPageChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response listServiceElectronicChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    List<ElectronicChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listElectronicChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response listServicePhoneChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    List<PhoneChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listPhoneChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response listServicePrintableFormChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    List<PrintableFormChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listPrintableFormChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response listServiceServiceLocationChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    List<ServiceLocationChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listServiceLocationChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response listServiceWebPageChannels(String serviceIdParam, Long firstResult, Long maxResults) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    if (serviceId == null) {
      return createBadRequest(String.format(INVALID_SERVICE_ID, serviceIdParam));
    }
    
    List<WebPageChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listWebPageChannelsChannels(serviceId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }

  @Override
  public Response updatePhoneChannel(String serviceId, String phoneChannelId, PhoneChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updatePrintableFormChannel(String serviceId, String printableFormChannelId,
      PrintableFormChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updateServiceElectronicChannel(String serviceId, String electronicChannelId, ElectronicChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updateServiceLocationChannel(String serviceId, String serviceLocationChannelId,
      ServiceLocationChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updateWebPageChannel(String serviceId, String webPageChannelId, WebPageChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  private ServiceId toServiceId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private List<ServiceProvider> getServiceProviders() {
    List<ServiceProvider> result = new ArrayList<>();
    
    Iterator<ServiceProvider> iterator = serviceProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
  private List<ServiceChannelProvider> getServiceChannelProviders() {
    List<ServiceChannelProvider> result = new ArrayList<>();
    
    Iterator<ServiceChannelProvider> iterator = serviceChannelProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }

}

