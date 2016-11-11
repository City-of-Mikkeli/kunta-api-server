package fi.otavanopisto.kuntaapi.server.controllers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.id.Id;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

@ApplicationScoped
public class HttpCacheController {
  
  @Inject
  private Logger logger;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  public Response getNotModified(Request request, Id id) {
    if (id == null) {
      return null;
    }
    
    if (!KuntaApiConsts.IDENTIFIER_NAME.equals(id.getSource())) {
      logger.log(Level.WARNING, String.format("Refused to pass id with source %s to modificationHashCache", id.getSource()));
      return null;
    }
    
    return getNotModified(request, id.getId());
  }
  
  public Response getNotModified(Request request, String id) {
    EntityTag tag = getEntityTag(id);
    if (tag == null) {
      return null;
    }
    
    ResponseBuilder builder = request.evaluatePreconditions(tag);
    if (builder != null) {
      return builder.build();
    }
   
    return null;
  }
  
  public Response getNotModified(Request request, List<String> ids) {
    EntityTag tag = getEntityTag(ids);
    if (tag == null) {
      return null;
    }
    
    ResponseBuilder builder = request.evaluatePreconditions(tag);
    if (builder != null) {
      return builder.build();
    }
   
    return null;
  }

  public Response sendModified(Object entity, String id) {
    EntityTag tag = getEntityTag(id);
    if (tag == null) {
      return Response.ok(entity).build();
    }
    
    CacheControl cacheControl = new CacheControl();
    cacheControl.setMustRevalidate(true);
    
    return Response.ok(entity)
      .cacheControl(cacheControl)
      .tag(tag)
      .build();
  }
  
  public Response sendModified(Object entity, List<String> ids) {
    EntityTag tag = getEntityTag(ids);
    if (tag == null) {
      return Response.ok(entity).build();
    }
    
    CacheControl cacheControl = new CacheControl();
    cacheControl.setMustRevalidate(true);
    
    return Response.ok(entity)
      .cacheControl(cacheControl)
      .tag(tag)
      .build();
  }
  
  public List<String> getEntityIds(List<?> entities) {
    if (entities.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<String> result = new ArrayList<>(entities.size());
    Class<? extends Object> entityClass = entities.get(0).getClass();
    
    try {
      Method getter = entityClass.getMethod("getId");
      
      for (Object entity : entities) {
        result.add((String) getter.invoke(entity));
      }
      
      return result;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to invoke getter", e);
    }
    
    return Collections.emptyList();
  }
  
  private EntityTag getEntityTag(String id) {
    String hash = modificationHashCache.get(id);
    if (hash == null) {
      return null;  
    }
    
    return new EntityTag(hash, true);
  }
  
  private EntityTag getEntityTag(List<String> ids) {
    for (String id : ids) {
      if (modificationHashCache.get(id) == null) {
        return null;
      }
    }
    
    return new EntityTag(DigestUtils.md5Hex(StringUtils.join(ids, '-')), true);
  }
  
}