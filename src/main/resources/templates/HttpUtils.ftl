package it.coopservice.api.util;

import org.jboss.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

public class HttpUtils {

   private static Logger logger = Logger.getLogger(HttpUtils.class);
   private static final String AUTHORIZATION_PROPERTY = "Authorization";
   private static final String AUTHORIZATION_PROPERTY_UPPER = "AUTHORIZATION";
   private static final String AUTHENTICATION_SCHEME = "Basic";
   private static final String AUTHENTICATION_SCHEME_UPPER = "BASIC";
   private static final String TOKEN_SCHEMA = "Bearer";
   private static final String TOKEN_SCHEMA_UPPER = "BEARER";
   private static final String X_FORWARDED_FOR = "X-FORWARDED-FOR";

   public static String[] getUsernamePassword(HttpHeaders headers) throws Exception
   {
      if (headers.getRequestHeader(AUTHORIZATION_PROPERTY) != null) {
         return getUsernamePassword(headers.getRequestHeader(AUTHORIZATION_PROPERTY));
      } else if (headers.getRequestHeader(AUTHORIZATION_PROPERTY_UPPER) != null) {
         return getUsernamePassword(headers.getRequestHeader(AUTHORIZATION_PROPERTY_UPPER));
      } else {
         return null;
      }

   }

   public static String[] getUsernamePassword(MultivaluedMap<String, String> headers) throws Exception
   {
      return getUsernamePassword(headers.get(AUTHORIZATION_PROPERTY));
   }

   public static String getRemoteIp(MultivaluedMap<String, String> headers) throws Exception
   {
      String complete = null;
      if (headers.containsKey(AUTHORIZATION_PROPERTY)) {
         complete = headers.getFirst(AUTHORIZATION_PROPERTY);
      } else if (headers.containsKey(AUTHORIZATION_PROPERTY_UPPER)) {
         complete = headers.getFirst(AUTHORIZATION_PROPERTY_UPPER);
      }

      if (complete != null) {
         if (complete.contains(TOKEN_SCHEMA)) {
            return complete.substring(complete.indexOf(TOKEN_SCHEMA) + TOKEN_SCHEMA.length() + 1).trim();
         } else if (complete.contains(TOKEN_SCHEMA_UPPER)) {
            return complete.substring(complete.indexOf(TOKEN_SCHEMA_UPPER) + TOKEN_SCHEMA_UPPER.length() + 1).trim();
         }
      }

      throw new Exception("no token in header");
   }


   private static String[] getUsernamePassword(List<String> authorization) throws Exception
   {

      // If no authorization information present; block access
      if (authorization == null || authorization.isEmpty()) {
         throw new Exception("authorization is empty");
      }

      // Get encoded username and password
      String encodedUserPassword = null;

      if (authorization.get(0).contains(AUTHENTICATION_SCHEME)) {
         encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");
      } else if (authorization.get(0).contains(AUTHENTICATION_SCHEME_UPPER)) {
         encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME_UPPER + " ", "");
      }

      // Decode username and password
      String usernameAndPassword;
      try {
         usernameAndPassword = new String(Base64.decode(encodedUserPassword));
      } catch (IOException e) {
         throw new Exception("error in decoding username and password");
      }

      // Split username and password tokens
      try {
         final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
         final String username = tokenizer.nextToken();
         final String password = tokenizer.nextToken();

         // Verifying Username and password
         logger.info(username);
         logger.info(password);
         return new String[]{username, password};
      } catch (Exception e) {
         throw new Exception("error in splitting username and password tokens");
      }

   }

   public static String parseFileName(MultivaluedMap<String, String> headers) {
      String[] contentDispositionHeader = headers.getFirst("Content-Disposition").split(";");
      for (String name : contentDispositionHeader) {
         if ((name.trim().startsWith("filename"))) {
            String[] tmp = name.split("=");
            return tmp[1].trim().replaceAll("\"", "");
         }
      }
      return null;
   }

}
