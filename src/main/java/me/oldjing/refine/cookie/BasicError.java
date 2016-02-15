package me.oldjing.refine.cookie;

public class BasicError {
	public static final int SUCCESS = 0; // success(customized defined)

	public static final int WEBAPI_ERR_UNKNOWN                      = 100; // Unknown error
	public static final int WEBAPI_ERR_BAD_REQUEST                  = 101; // Invalid parameters
	public static final int WEBAPI_ERR_NO_SUCH_API                  = 102; // API does not exist
	public static final int WEBAPI_ERR_NO_SUCH_METHOD               = 103; // Method does not exist
	public static final int WEBAPI_ERR_NOT_SUPPORTED_VERSION        = 104; // This API version is not supported
	public static final int WEBAPI_ERR_NO_PERMISSION                = 105; // Insufficient user privilege
	public static final int WEBAPI_ERR_SESSION_TIMEOUT              = 106; // Connection time out
	public static final int WEBAPI_ERR_SESSION_INTERRUPT            = 107; // Multiple login detected
	public static final int WEBAPI_ERR_HANDLE_UPLOAD                = 108;
	public static final int WEBAPI_ERR_PROCESS_RELAY                = 109;
	public static final int WEBAPI_ERR_PROCESS_ENTRY                = 110;
	public static final int WEBAPI_ERR_PROCESS_LIB                  = 111;
	public static final int WEBAPI_ERR_COMPOUND_STOP                = 112;
	public static final int WEBAPI_ERR_COMPOUND_REJECT              = 113;
	public static final int WEBAPI_ERR_NO_REQUIRED_PARAM            = 114;
	public static final int WEBAPI_ERR_NOT_ALLOW_UPLOAD             = 115;
	public static final int WEBAPI_ERR_NOT_ALLOW_DEMO               = 116;
	public static final int WEBAPI_ERR_INTERNAL_ERROR               = 117;
	public static final int WEBAPI_ERR_PROCESS_NAME_ERROR           = 118;
	public static final int WEBAPI_ERR_SID_NOT_FOUND                = 119; // Insufficient user privilege
	public static final int WEBAPI_ERR_REQUEST_PARAMETER_INVALID    = 120;
	public static final int WEBAPI_ERR_NO_MATCH_LIB_ENTRY           = 121;
	public static final int WEBAPI_ERR_SHARING_SID_NOT_FOUND        = 122;
	public static final int WEBAPI_ERR_SHARING_ERROR_TOKEN          = 123;
	public static final int WEBAPI_ERR_SHARING_INVALID_ENTRY        = 124;
	public static final int WEBAPI_ERR_SHARING_TIMEOUT              = 125;
	public static final int WEBAPI_ERR_SHARING_NO_PERMISSION        = 126;
	public static final int WEBAPI_ERR_SHARING_NO_APP_PERMISSION    = 127;
	public static final int WEBAPI_ERR_IP_NOT_MATCHED               = 150;

}
