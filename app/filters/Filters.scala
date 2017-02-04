package filters

import javax.inject.Inject

import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

class Filters @Inject() (gzip: GzipFilter, security: SecurityHeadersFilter) extends HttpFilters {
  var filters: Seq[EssentialFilter] = Seq(gzip, security)
}