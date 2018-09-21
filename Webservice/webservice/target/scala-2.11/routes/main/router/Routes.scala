
// @GENERATOR:play-routes-compiler
// @SOURCE:C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/conf/routes
// @DATE:Wed Mar 28 11:05:28 CEST 2018

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:6
  Application_1: controllers.Application,
  // @LINE:7
  ExtendTable_3: controllers.ExtendTable,
  // @LINE:9
  Assets_0: controllers.Assets,
  // @LINE:23
  ExtenededSearch_2: extendedSearch2.ExtenededSearch,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:6
    Application_1: controllers.Application,
    // @LINE:7
    ExtendTable_3: controllers.ExtendTable,
    // @LINE:9
    Assets_0: controllers.Assets,
    // @LINE:23
    ExtenededSearch_2: extendedSearch2.ExtenededSearch
  ) = this(errorHandler, Application_1, ExtendTable_3, Assets_0, ExtenededSearch_2, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, Application_1, ExtendTable_3, Assets_0, ExtenededSearch_2, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """controllers.Application.index()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """ind""", """controllers.ExtendTable.ind()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/$file<.+>""", """controllers.Assets.versioned(path:String = "/public", file:Asset)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """getRepositoryNames""", """controllers.ExtendTable.getRepositoryNames()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """getRepositoryStatistics""", """controllers.ExtendTable.getRepositoryStatistics(repository:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """suggestAttributes""", """controllers.ExtendTable.suggestAttributes()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """old_search""", """controllers.ExtendTable.search()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """search""", """controllers.ExtendTable.extendedSearch(repository:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """unconstrainedSearch""", """controllers.ExtendTable.unconstrainedSearch(repository:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """extendedSearch""", """extendedSearch2.ExtenededSearch.extendedSearch(repository:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """correlationBasedSearch""", """controllers.ExtendTable.correlationBasedSearch(repository:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """fetchTable""", """controllers.ExtendTable.fetchTable(name:String, repository:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """fetchTablePOST""", """controllers.ExtendTable.fetchTablePOST(repositoryName:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """createRepository""", """controllers.ExtendTable.createRepository(repository:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """createRepository""", """controllers.ExtendTable.createRepository(repository:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """uploadTable""", """controllers.ExtendTable.uploadTable(repository:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """bulkUploadTables""", """controllers.ExtendTable.moderateBulkUploadTables(repository:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """deleteRepository""", """controllers.ExtendTable.deleteRepository(repository:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """getUploadStatus""", """controllers.ExtendTable.getUploadStatus(repository:String, uploadID:String)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:6
  private[this] lazy val controllers_Application_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_Application_index0_invoker = createInvoker(
    Application_1.index(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "index",
      Nil,
      "GET",
      """ functions defined by Java Play""",
      this.prefix + """"""
    )
  )

  // @LINE:7
  private[this] lazy val controllers_ExtendTable_ind1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("ind")))
  )
  private[this] lazy val controllers_ExtendTable_ind1_invoker = createInvoker(
    ExtendTable_3.ind(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "ind",
      Nil,
      "GET",
      """""",
      this.prefix + """ind"""
    )
  )

  // @LINE:9
  private[this] lazy val controllers_Assets_versioned2_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_versioned2_invoker = createInvoker(
    Assets_0.versioned(fakeValue[String], fakeValue[Asset]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "versioned",
      Seq(classOf[String], classOf[Asset]),
      "GET",
      """ Map static resources from the /public folder to the /assets URL path""",
      this.prefix + """assets/$file<.+>"""
    )
  )

  // @LINE:12
  private[this] lazy val controllers_ExtendTable_getRepositoryNames3_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("getRepositoryNames")))
  )
  private[this] lazy val controllers_ExtendTable_getRepositoryNames3_invoker = createInvoker(
    ExtendTable_3.getRepositoryNames(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "getRepositoryNames",
      Nil,
      "GET",
      """ Get info functions""",
      this.prefix + """getRepositoryNames"""
    )
  )

  // @LINE:13
  private[this] lazy val controllers_ExtendTable_getRepositoryStatistics4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("getRepositoryStatistics")))
  )
  private[this] lazy val controllers_ExtendTable_getRepositoryStatistics4_invoker = createInvoker(
    ExtendTable_3.getRepositoryStatistics(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "getRepositoryStatistics",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """getRepositoryStatistics"""
    )
  )

  // @LINE:14
  private[this] lazy val controllers_ExtendTable_suggestAttributes5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("suggestAttributes")))
  )
  private[this] lazy val controllers_ExtendTable_suggestAttributes5_invoker = createInvoker(
    ExtendTable_3.suggestAttributes(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "suggestAttributes",
      Nil,
      "POST",
      """""",
      this.prefix + """suggestAttributes"""
    )
  )

  // @LINE:18
  private[this] lazy val controllers_ExtendTable_search6_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("old_search")))
  )
  private[this] lazy val controllers_ExtendTable_search6_invoker = createInvoker(
    ExtendTable_3.search(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "search",
      Nil,
      "POST",
      """ table search related functions""",
      this.prefix + """old_search"""
    )
  )

  // @LINE:19
  private[this] lazy val controllers_ExtendTable_extendedSearch7_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("search")))
  )
  private[this] lazy val controllers_ExtendTable_extendedSearch7_invoker = createInvoker(
    ExtendTable_3.extendedSearch(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "extendedSearch",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """search"""
    )
  )

  // @LINE:22
  private[this] lazy val controllers_ExtendTable_unconstrainedSearch8_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("unconstrainedSearch")))
  )
  private[this] lazy val controllers_ExtendTable_unconstrainedSearch8_invoker = createInvoker(
    ExtendTable_3.unconstrainedSearch(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "unconstrainedSearch",
      Seq(classOf[String]),
      "POST",
      """ POST /search 						controllers.ExtendTable.extendedSearch_Produktdata()
 POST /search 						controllers.ExtendTable.PreCalculatedSearch()""",
      this.prefix + """unconstrainedSearch"""
    )
  )

  // @LINE:23
  private[this] lazy val extendedSearch2_ExtenededSearch_extendedSearch9_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("extendedSearch")))
  )
  private[this] lazy val extendedSearch2_ExtenededSearch_extendedSearch9_invoker = createInvoker(
    ExtenededSearch_2.extendedSearch(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "extendedSearch2.ExtenededSearch",
      "extendedSearch",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """extendedSearch"""
    )
  )

  // @LINE:26
  private[this] lazy val controllers_ExtendTable_correlationBasedSearch10_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("correlationBasedSearch")))
  )
  private[this] lazy val controllers_ExtendTable_correlationBasedSearch10_invoker = createInvoker(
    ExtendTable_3.correlationBasedSearch(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "correlationBasedSearch",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """correlationBasedSearch"""
    )
  )

  // @LINE:30
  private[this] lazy val controllers_ExtendTable_fetchTable11_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("fetchTable")))
  )
  private[this] lazy val controllers_ExtendTable_fetchTable11_invoker = createInvoker(
    ExtendTable_3.fetchTable(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "fetchTable",
      Seq(classOf[String], classOf[String]),
      "GET",
      """ GET /fetchTable  					controllers.ExtendTable.fetchTable_T2DGoldstandard(name)""",
      this.prefix + """fetchTable"""
    )
  )

  // @LINE:33
  private[this] lazy val controllers_ExtendTable_fetchTablePOST12_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("fetchTablePOST")))
  )
  private[this] lazy val controllers_ExtendTable_fetchTablePOST12_invoker = createInvoker(
    ExtendTable_3.fetchTablePOST(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "fetchTablePOST",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """fetchTablePOST"""
    )
  )

  // @LINE:36
  private[this] lazy val controllers_ExtendTable_createRepository13_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("createRepository")))
  )
  private[this] lazy val controllers_ExtendTable_createRepository13_invoker = createInvoker(
    ExtendTable_3.createRepository(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "createRepository",
      Seq(classOf[String]),
      "POST",
      """ Build and maintain repositories""",
      this.prefix + """createRepository"""
    )
  )

  // @LINE:37
  private[this] lazy val controllers_ExtendTable_createRepository14_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("createRepository")))
  )
  private[this] lazy val controllers_ExtendTable_createRepository14_invoker = createInvoker(
    ExtendTable_3.createRepository(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "createRepository",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """createRepository"""
    )
  )

  // @LINE:38
  private[this] lazy val controllers_ExtendTable_uploadTable15_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("uploadTable")))
  )
  private[this] lazy val controllers_ExtendTable_uploadTable15_invoker = createInvoker(
    ExtendTable_3.uploadTable(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "uploadTable",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """uploadTable"""
    )
  )

  // @LINE:39
  private[this] lazy val controllers_ExtendTable_moderateBulkUploadTables16_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("bulkUploadTables")))
  )
  private[this] lazy val controllers_ExtendTable_moderateBulkUploadTables16_invoker = createInvoker(
    ExtendTable_3.moderateBulkUploadTables(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "moderateBulkUploadTables",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """bulkUploadTables"""
    )
  )

  // @LINE:40
  private[this] lazy val controllers_ExtendTable_deleteRepository17_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("deleteRepository")))
  )
  private[this] lazy val controllers_ExtendTable_deleteRepository17_invoker = createInvoker(
    ExtendTable_3.deleteRepository(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "deleteRepository",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """deleteRepository"""
    )
  )

  // @LINE:42
  private[this] lazy val controllers_ExtendTable_getUploadStatus18_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("getUploadStatus")))
  )
  private[this] lazy val controllers_ExtendTable_getUploadStatus18_invoker = createInvoker(
    ExtendTable_3.getUploadStatus(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ExtendTable",
      "getUploadStatus",
      Seq(classOf[String], classOf[String]),
      "POST",
      """""",
      this.prefix + """getUploadStatus"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:6
    case controllers_Application_index0_route(params) =>
      call { 
        controllers_Application_index0_invoker.call(Application_1.index())
      }
  
    // @LINE:7
    case controllers_ExtendTable_ind1_route(params) =>
      call { 
        controllers_ExtendTable_ind1_invoker.call(ExtendTable_3.ind())
      }
  
    // @LINE:9
    case controllers_Assets_versioned2_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[Asset]("file", None)) { (path, file) =>
        controllers_Assets_versioned2_invoker.call(Assets_0.versioned(path, file))
      }
  
    // @LINE:12
    case controllers_ExtendTable_getRepositoryNames3_route(params) =>
      call { 
        controllers_ExtendTable_getRepositoryNames3_invoker.call(ExtendTable_3.getRepositoryNames())
      }
  
    // @LINE:13
    case controllers_ExtendTable_getRepositoryStatistics4_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        controllers_ExtendTable_getRepositoryStatistics4_invoker.call(ExtendTable_3.getRepositoryStatistics(repository))
      }
  
    // @LINE:14
    case controllers_ExtendTable_suggestAttributes5_route(params) =>
      call { 
        controllers_ExtendTable_suggestAttributes5_invoker.call(ExtendTable_3.suggestAttributes())
      }
  
    // @LINE:18
    case controllers_ExtendTable_search6_route(params) =>
      call { 
        controllers_ExtendTable_search6_invoker.call(ExtendTable_3.search())
      }
  
    // @LINE:19
    case controllers_ExtendTable_extendedSearch7_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        controllers_ExtendTable_extendedSearch7_invoker.call(ExtendTable_3.extendedSearch(repository))
      }
  
    // @LINE:22
    case controllers_ExtendTable_unconstrainedSearch8_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        controllers_ExtendTable_unconstrainedSearch8_invoker.call(ExtendTable_3.unconstrainedSearch(repository))
      }
  
    // @LINE:23
    case extendedSearch2_ExtenededSearch_extendedSearch9_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        extendedSearch2_ExtenededSearch_extendedSearch9_invoker.call(ExtenededSearch_2.extendedSearch(repository))
      }
  
    // @LINE:26
    case controllers_ExtendTable_correlationBasedSearch10_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        controllers_ExtendTable_correlationBasedSearch10_invoker.call(ExtendTable_3.correlationBasedSearch(repository))
      }
  
    // @LINE:30
    case controllers_ExtendTable_fetchTable11_route(params) =>
      call(params.fromQuery[String]("name", None), params.fromQuery[String]("repository", None)) { (name, repository) =>
        controllers_ExtendTable_fetchTable11_invoker.call(ExtendTable_3.fetchTable(name, repository))
      }
  
    // @LINE:33
    case controllers_ExtendTable_fetchTablePOST12_route(params) =>
      call(params.fromQuery[String]("repositoryName", None)) { (repositoryName) =>
        controllers_ExtendTable_fetchTablePOST12_invoker.call(ExtendTable_3.fetchTablePOST(repositoryName))
      }
  
    // @LINE:36
    case controllers_ExtendTable_createRepository13_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        controllers_ExtendTable_createRepository13_invoker.call(ExtendTable_3.createRepository(repository))
      }
  
    // @LINE:37
    case controllers_ExtendTable_createRepository14_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        controllers_ExtendTable_createRepository14_invoker.call(ExtendTable_3.createRepository(repository))
      }
  
    // @LINE:38
    case controllers_ExtendTable_uploadTable15_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        controllers_ExtendTable_uploadTable15_invoker.call(ExtendTable_3.uploadTable(repository))
      }
  
    // @LINE:39
    case controllers_ExtendTable_moderateBulkUploadTables16_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        controllers_ExtendTable_moderateBulkUploadTables16_invoker.call(ExtendTable_3.moderateBulkUploadTables(repository))
      }
  
    // @LINE:40
    case controllers_ExtendTable_deleteRepository17_route(params) =>
      call(params.fromQuery[String]("repository", None)) { (repository) =>
        controllers_ExtendTable_deleteRepository17_invoker.call(ExtendTable_3.deleteRepository(repository))
      }
  
    // @LINE:42
    case controllers_ExtendTable_getUploadStatus18_route(params) =>
      call(params.fromQuery[String]("repository", None), params.fromQuery[String]("uploadID", None)) { (repository, uploadID) =>
        controllers_ExtendTable_getUploadStatus18_invoker.call(ExtendTable_3.getUploadStatus(repository, uploadID))
      }
  }
}