package fixtures;

import io.github.byreshb.avl.annotation.Layer;
import io.github.byreshb.avl.annotation.Team;
import io.github.byreshb.avl.annotation.TestLayer;
import org.junit.jupiter.api.Test;

@Layer(TestLayer.API)
@Team("payments")
public class CompliantTest {

  @Test
  public void appliesPromoCodeToTotal() {}

  @Team("checkout")
  @Test
  public void methodOverridesClassTeam() {}
}
