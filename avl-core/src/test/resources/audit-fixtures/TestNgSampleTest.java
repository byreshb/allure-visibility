package fixtures;

import io.github.byreshb.avl.annotation.Team;
import org.testng.annotations.Test;

public class TestNgSampleTest {

  @Team("platform")
  @Test
  public void missingLayerOnly() {}
}
