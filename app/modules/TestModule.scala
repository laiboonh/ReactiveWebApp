package modules

import org.specs2.mock.Mockito
import play.api.libs.ws.WSClient
import scaldi.Module

class TestModule extends Module with Mockito {

  val mockWS = mock[WSClient]
  bind [WSClient] to mockWS
}
