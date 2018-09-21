
// @GENERATOR:play-routes-compiler
// @SOURCE:C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/conf/routes
// @DATE:Wed Mar 28 11:05:28 CEST 2018

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:6
package controllers {

  // @LINE:7
  class ReverseExtendTable(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:19
    def extendedSearch(repository:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "search" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)))))
    }
  
    // @LINE:33
    def fetchTablePOST(repositoryName:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "fetchTablePOST" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repositoryName", repositoryName)))))
    }
  
    // @LINE:7
    def ind(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "ind")
    }
  
    // @LINE:14
    def suggestAttributes(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "suggestAttributes")
    }
  
    // @LINE:40
    def deleteRepository(repository:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "deleteRepository" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)))))
    }
  
    // @LINE:39
    def moderateBulkUploadTables(repository:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "bulkUploadTables" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)))))
    }
  
    // @LINE:22
    def unconstrainedSearch(repository:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "unconstrainedSearch" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)))))
    }
  
    // @LINE:26
    def correlationBasedSearch(repository:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "correlationBasedSearch" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)))))
    }
  
    // @LINE:36
    def createRepository(repository:String): Call = {
    
      (repository: @unchecked) match {
      
        // @LINE:36
        case (repository)  =>
          import ReverseRouteContext.empty
          Call("POST", _prefix + { _defaultPrefix } + "createRepository" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)))))
      
      }
    
    }
  
    // @LINE:13
    def getRepositoryStatistics(repository:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "getRepositoryStatistics" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)))))
    }
  
    // @LINE:30
    def fetchTable(name:String, repository:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "fetchTable" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("name", name)), Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)))))
    }
  
    // @LINE:42
    def getUploadStatus(repository:String, uploadID:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "getUploadStatus" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)), Some(implicitly[QueryStringBindable[String]].unbind("uploadID", uploadID)))))
    }
  
    // @LINE:18
    def search(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "old_search")
    }
  
    // @LINE:38
    def uploadTable(repository:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "uploadTable" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("repository", repository)))))
    }
  
    // @LINE:12
    def getRepositoryNames(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "getRepositoryNames")
    }
  
  }

  // @LINE:9
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:9
    def versioned(file:Asset): Call = {
      implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
      Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[Asset]].unbind("file", file))
    }
  
  }

  // @LINE:6
  class ReverseApplication(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:6
    def index(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix)
    }
  
  }


}