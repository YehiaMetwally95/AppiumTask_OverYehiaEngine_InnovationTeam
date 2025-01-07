package yehiaEngine.managers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import yehiaEngine.loggers.AllureReportLogger;
import yehiaEngine.loggers.LogHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static yehiaEngine.loggers.LogHelper.logInfoStep;
import static yehiaEngine.managers.JsonManager.convertMapToJsonObject;

public class ApisManager {
    @AllArgsConstructor
    @Getter
    public enum MethodType {
        POST("Post"),
        GET("Get"),
        PUT("Put"),
        PATCH("Patch"),
        DELETE("Delete");
        final String methodType;
    }

    @AllArgsConstructor
    @Getter
    public enum ContentType {
        JSON("application/json"),
        URLENCODED("application/x-www-form-urlencoded");
        final String contentType;
    }

    @AllArgsConstructor
    @Getter
    public enum AuthType {
        BasicAuth("BasicAuth"),
        BearerToken("BearerToken"),
        CookieAuth("CookieAuth"),
        XAuthToken("X-Auth-Token"),
        SessionID("Session-ID");

        final String authType;
    }

    @AllArgsConstructor
    @Getter
    public enum ParameterType {
        QUERY("queryParameter"),
        PATH("pathParameter"),
        NULL(null);
        final String parameterType;
    }


    public static Response MakeRequest(MethodType methodType, String endpoint, Object requestBody, ContentType requestContentType) {
        try {
            RestAssured.registerParser("text/html", Parser.JSON);
            //Upload Request and Response Into Allure Report
            RequestSpecification request = RestAssured.given().filter(AllureReportLogger.logApiRequestsToAllureReport());
            Response response = null;

            //Set Content Type of Request
            if (requestContentType.getContentType() != null) {
                if (requestContentType == ContentType.JSON) {
                    request = request.contentType(requestContentType.getContentType());
                } else if (requestContentType == ContentType.URLENCODED) {
                    //convert requestObject to Map
                    Map<String, Object> map = JsonManager.convertJsonStringToMap(requestBody);
                    request = request.contentType(requestContentType.getContentType() + "; charset=utf-8").formParams(map);
                }
            }

            //Set The Request Body
            if (requestBody != null)
                request = request.body(requestBody);

            //Set Method Type of Request
            response = switch (methodType.getMethodType()) {
                case "Post" -> request.when().post(endpoint);
                case "Put" -> request.when().put(endpoint);
                case "Patch" -> request.when().patch(endpoint);
                case "Delete" -> request.when().delete(endpoint);
                default -> response;
            };

            logInfoStep("Sending " + methodType.getMethodType() + " Request for URL [" + endpoint + "]");

            //Convert Request body and Response body to Objects
            assert response != null;
            Object responseBody = JsonParser.parseString(getResponseBody(response)).getAsJsonObject();
            String requestString = new JsonMapper().writeValueAsString(requestBody);
            requestBody = JsonParser.parseString(requestString).getAsJsonObject();

            //Log Request Body and Response Body
            logRequestBody(requestBody);
            logResponseBody(responseBody);

            return response;
        } catch (Exception e) {
            LogHelper.logErrorStep("Failed to Send " + methodType.getMethodType() + " Request for URL [" + endpoint + "]", e);
            return null;
        }
    }

    public static Response MakeAuthRequest(MethodType methodType, String endpoint, Object requestBody, ContentType requestContentType,
                                           AuthType authType, String token, String authUser, String authPass) {
        try {
            RestAssured.registerParser("text/html", Parser.JSON);
            //Upload Request and Response Into Allure Report
            RequestSpecification request = RestAssured.given().filter(AllureReportLogger.logApiRequestsToAllureReport());
            Response response = null;

            //Set Content Type of Request
            if (requestContentType != null) {
                if (requestContentType == ContentType.JSON) {
                    request = request.contentType(requestContentType.getContentType());
                } else if (requestContentType == ContentType.URLENCODED) {
                    //convert requestObject to Map
                    Map<String, Object> map = JsonManager.convertJsonStringToMap(requestBody);
                    request = request.contentType(requestContentType.getContentType() + "; charset=utf-8").formParams(map);
                }
            }

            //Set The Request Body
            if (requestBody != null)
                request = request.body(requestBody);

            //Set Authentication Type of Request
            if (authType == AuthType.BasicAuth) {
                request = request.auth().basic(authUser, authPass);
            } else if (authType == AuthType.BearerToken) {
                request = request.header("Authorization", "Bearer " + token);
            } else if (authType == AuthType.CookieAuth) {
                request = request.header("Cookie", "token="+token);
            } else if (authType == AuthType.XAuthToken) {
                request = request.header("x-auth-token", token);
            } else if (authType == AuthType.SessionID) {
                request = request.header("Session-ID", token);
            }

            //Set Method Type of Request
            response = switch (methodType.getMethodType()) {
                case "Post" -> request.when().post(endpoint);
                case "Put" -> request.when().put(endpoint);
                case "Patch" -> request.when().patch(endpoint);
                case "Delete" -> request.when().delete(endpoint);
                default -> response;
            };
            logInfoStep("Sending Auth " + methodType.getMethodType() + " Request for URL [" + endpoint + "]");

            //Convert Request body and Response body to Objects
            assert response != null;
            Object responseBody= JsonParser.parseString(getResponseBody(response));
            if (responseBody instanceof JsonObject)
                ((JsonObject) responseBody).getAsJsonObject();

            String requestString = new JsonMapper().writeValueAsString(requestBody);
            requestBody = JsonParser.parseString(requestString);
            if (requestBody instanceof JsonObject)
                ((JsonObject) requestBody).getAsJsonObject();


                //Log Request Body and Response Body
            logRequestBody(requestBody);
            logResponseBody(responseBody);
            return response;
        } catch (Exception e) {
            LogHelper.logErrorStep("Failed to Send Auth " + methodType.getMethodType() + " Request for URL [" + endpoint + "]", e);
            return null;
        }
    }

    public static Response GetRequest(String endpoint, ParameterType parameterType, Map<String,?> parameters) {
        try {
            //Upload Request and Response Into Allure Report
            RequestSpecification request = RestAssured.given().filter(AllureReportLogger.logApiRequestsToAllureReport());
            Response response = null;

            if (parameterType == ParameterType.QUERY){
                //Set Parameter Type of Request
                request = request.queryParams(parameters);
                //Perform Get Request
                response = request.when().get(endpoint);
            }

            else if (parameterType == ParameterType.PATH){
                //Set Parameter Type of Request
                request = request.pathParams(parameters);
                //Perform Get Request
                response = request.when().get(endpoint+"/{"+parameters.keySet().iterator().next()+"}");
            }

            logInfoStep("Sending Get Request for URL [" + endpoint + "]");

            if (parameterType == ParameterType.QUERY || parameterType == ParameterType.PATH) {
                //Convert Response body to Object
                Object requestBody = convertMapToJsonObject(parameters);
                //Log Response Body
                logRequestBody(requestBody);
            }
            
            Object responseBody = null;
            //Convert Request body to Objects
            if (isJsonString(getResponseBody(response)))
                responseBody= JsonParser.parseString(getResponseBody(response));
            if (responseBody instanceof JsonObject)
                ((JsonObject) responseBody).getAsJsonObject();

            logResponseBody(responseBody);

            return response;
        } catch (Exception e) {
            LogHelper.logErrorStep("Failed to Send Get Request for URL [" + endpoint + "]", e);
            return null;
        }
    }

    public static Response GetAuthRequest(String endpoint, ParameterType parameterType, Map<String,?> parameters,
                                          AuthType authType, String token, String authUser, String authPass) {
        try {
            //Upload Request and Response Into Allure Report
            RequestSpecification request = RestAssured.given().filter(AllureReportLogger.logApiRequestsToAllureReport());
            Response response = null;

            //Set Content Type of Request
            if (parameterType == ParameterType.QUERY)
                request = request.queryParams(parameters);

            else if (parameterType == ParameterType.PATH)
                request = request.pathParams(parameters);

            //Set Authentication Type of Request
            if (authType == AuthType.BasicAuth) {
                request = request.auth().basic(authUser, authPass);
            } else if (authType == AuthType.BearerToken) {
                request = request.header("Authorization", "Bearer " + token);
            } else if (authType == AuthType.CookieAuth) {
                request = request.header("Cookie", token);
            } else if (authType == AuthType.XAuthToken) {
                request = request.header("x-auth-token", token);
            } else if (authType == AuthType.SessionID) {
                request = request.header("Session-ID", token);
            }

            //Perform Get Request
            response = request.when().get(endpoint);
            logInfoStep("Sending Auth Get Request for URL [" + endpoint + "]");

            if (parameterType == ParameterType.QUERY || parameterType == ParameterType.PATH) {
                //Convert Response body to Object
                Object requestBody = convertMapToJsonObject(parameters);
                //Log Response Body
                logRequestBody(requestBody);
            }

            //Convert Request body to Objects
            Object responseBody = null;
            //Convert Request body to Objects
            if (isJsonString(getResponseBody(response)))
                responseBody= JsonParser.parseString(getResponseBody(response));
            if (responseBody instanceof JsonObject)
                ((JsonObject) responseBody).getAsJsonObject();

            //Log Request Body
            logResponseBody(responseBody);

            return response;
        } catch (Exception e) {
            LogHelper.logErrorStep("Failed to Send Auth Get Request for URL [" + endpoint + "]", e);
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    public static int getJsonIntValueFromResponse(Response response, String jsonPath) {
        return response.jsonPath().getInt(jsonPath);
    }

    public static String getJsonStringValueFromResponse(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }

    public static JsonObject getJsonObjectFromResponse(Response response, String jsonPath) {
        return response.jsonPath().getJsonObject(jsonPath);
    }

    public static List<Object> getListOfObjectsFromResponse(Response response, String jsonPath) {
        return response.jsonPath().getList(jsonPath);
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    public static void logRequestBody(Object request) {
        try {
            logInfoStep("Request Body : " + request.toString());
            System.out.println("*********************************************");
        } catch (Exception e) {
            LogHelper.logErrorStep("Failed to Log the Request Body", e);
        }
    }

    public static void logResponseBody(Object response) {
        try {
            logInfoStep("Response Body : " + response);
            System.out.println("*********************************************\n");
        } catch (Exception e) {
            LogHelper.logErrorStep("Failed to Log the Response Body", e);
        }
    }

    public static String getResponseBody(Response response) {
        return response.getBody().asPrettyString();
    }

    public static List<Header> getResponseHeaders(Response response) {
        return response.getHeaders().asList();
    }

    public static String getResponseHeaderByName(Response response, String headerName) {
        return response.getHeader(headerName);
    }

    public static Map getResponseCookies(Response response) {
        return response.getCookies();
    }

    public static long getResponseTime(Response response, TimeUnit timeUnit) {
        return response.getTimeIn(timeUnit);
    }

    public static int getResponseCode(Response response) {
        return response.statusCode();
    }

    public static String getResponseContentType(Response response, String contentType) {
        return response.getContentType();
    }

    public static boolean isJsonString(String input) {
        try {
            JsonElement jsonElement = JsonParser.parseString(input);
            // Successfully parsed
            return true;
        } catch (JsonSyntaxException e) {
            // Failed to parse
            return false;
        }
    }

}
