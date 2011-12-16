/*
   PACHINKO, A fast, embeddable micro-rule engine runtime.
   Version 0.2

   Copyright 2011 Kenneth R. Mackenzie (www.mackenzieresearch.com)

   This program is free software: you can redistribute it and/or modify
   it under the terms of Version 3 of the GNU Affero General Public
   License as published by the Free Software Foundation.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   If you require a version of this software which can be used as
   part of a commercial for-profit program, please contact Mackenzie
   Research for a commercial license.

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.

   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.mackenzieresearch.pachinko.test.rule;

import com.mackenzieresearch.pachinko.CARuleSystem;
import com.mackenzieresearch.pachinko.DefaultCARule;
import com.mackenzieresearch.pachinko.Variable;
import com.mackenzieresearch.roux.kernel.IMonad;
import com.mackenzieresearch.roux.kernel.IMonadex;
import com.mackenzieresearch.roux.kernel.IReadOnlyMonad;
import com.mackenzieresearch.roux.kernel.IReadWriteMonadex;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static util.RuntimeAssertion.assertTrue;


public class PKScalingPerfTest {
  public class PKSelectRule extends DefaultCARule<Integer> {
    Integer _keyValue = 0;
    //  Remember these to avoid the cost of hash lookups on each rule evaluation...
    int _event = -1;
    int _status = -1;

    public PKSelectRule(int keyValueForThisRule) {
      super();
      _event = addPkVariable(new Variable<Integer>("EVENT", 0), keyValueForThisRule);
      _status = addOptionalVariable(new Variable<Integer>("STATUS", 0));
      _keyValue = keyValueForThisRule;
    }

    @Override
    public boolean evaluateCondition(IMonadex<? extends IReadOnlyMonad> context) {
      //  No need to test for anything, the PK mechanism does it for us:
      return true;
    }

    @Override
    public void doAction(IReadWriteMonadex<? extends IMonad> context) {
      context.returnValue(_status, _keyValue);
    }
  }

  @Test
  public void pkFunctionalTest() {
    //  Initialize rule system with its set of rules:
    List<PKSelectRule> rules = new ArrayList<PKSelectRule>();
    for (int x = 1; x <= 5; x++)
      rules.add(new PKSelectRule(x));
    CARuleSystem<Integer> ruleSystem = new CARuleSystem<Integer>(rules.toArray(new PKSelectRule[rules.size()]));

    IReadWriteMonadex<IMonad<Integer>> context = ruleSystem.freeVariables();

    //  Ensure that rule #3 fires when the EVENT is 3...
    context.returnValue("EVENT", 3);
    ruleSystem.executeActivations();
    assertTrue(((Integer) context.bindValue("STATUS")) == 3);

    //  Second try, to make sure it's repeatable...
    context.returnValue("EVENT", 4);
    ruleSystem.executeActivations();
    assertTrue(((Integer) context.bindValue("STATUS")) == 4);
  }

  @Test
  public void pk1000RuleTest() {
    //  Initialize rule system with its set of rules:
    List<PKSelectRule> rules = new ArrayList<PKSelectRule>();
    for (int x = 1; x <= 1000; x++)
      rules.add(new PKSelectRule(x));
    CARuleSystem<Integer> ruleSystem = new CARuleSystem<Integer>(rules.toArray(new PKSelectRule[rules.size()]));

    IReadWriteMonadex<IMonad<Integer>> context = ruleSystem.freeVariables();
    int EVENT = context.getIndex("EVENT");

    //  Warm up the hotspot compiler...
    int iterations = 100000;
    for (int i = 0; i < iterations; i++) {
      context.returnValue(EVENT, 27);
      ruleSystem.executeActivations();
    }

    //  Now make our measurement...
    iterations = 100000;
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      context.returnValue(EVENT, 27);
      ruleSystem.executeActivations();
    }
    long elapsedTime = System.nanoTime() - startTime;
    System.out.println("One hundred thousand iterations, in nanos: " + elapsedTime);
    System.out.println("Average for one iteration, in nanos: " + (((double) elapsedTime) / ((double) iterations)));
  }

}
