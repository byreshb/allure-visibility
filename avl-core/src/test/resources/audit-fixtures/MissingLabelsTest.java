package fixtures;

import io.github.byreshb.avl.annotation.Layer;
import io.github.byreshb.avl.annotation.TestLayer;
import org.junit.jupiter.api.Test;

@Layer(TestLayer.UNIT)
public class MissingLabelsTest {

  @Test
  public void missingTeamOnly() {}

  @Test
  public void alsoMissingTeam() {}
}
