package modules

import scaldi.Module
import services._

class MainModule extends Module {
  
  bind[TwitterService] to new DefaultTwitterService
  bind[StatisticsRepository] to new MongoStatisticsRepository
  bind[StatisticsService] to new
    DefaultStatisticsService(inject[StatisticsRepository], inject[TwitterService])

}
