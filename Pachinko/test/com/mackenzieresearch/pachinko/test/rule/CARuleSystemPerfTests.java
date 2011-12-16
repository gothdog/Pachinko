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

import static util.RuntimeAssertion.assertEqual;


public class CARuleSystemPerfTests {

  //--------------------------------------------------------------------------------------------------------------------------
  //
  //  Sample rule:
  //
  //    if (EVENT == "StartEvent")
  //      STATUS <- "STARTED"
  //
  public class StartEventRule extends DefaultCARule<String> {
    int _event = -1;
    int _status = -1;

    public StartEventRule() {
      super();
      _event = addPkVariable(new Variable<String>("EVENT", "StartEvent"), "StartEvent");
      _status = addOptionalVariable(new Variable("STATUS", "NOT_STARTED"));
    }

    @Override
    public boolean evaluateCondition(IMonadex<? extends IReadOnlyMonad> context) {
      return context.bindValue(_event).equals("StartEvent");
    }

    @Override
    public void doAction(IReadWriteMonadex<? extends IMonad> context) {
      context.returnValue(_status, "STARTED");
    }
  }

  //--------------------------------------------------------------------------------------------------------------------------
  //
  //  Set up rule system to use sample rule and then verify results...
  //
  @Test
  public void simpleStartEventTest() {
    //  Initialize rule system with its set of rules:
    CARuleSystem<String> ruleSystem = new CARuleSystem<String>(new StartEventRule());

    //  Set some data into the rule system and process any resulting rule activations...
    IReadWriteMonadex readWriteContext = ruleSystem.freeVariables();
    readWriteContext.returnValue("EVENT", "IdleEvent");
    ruleSystem.executeActivations();

    //  Verify that the rule system did not change state...
    assertEqual("NOT_STARTED", readWriteContext.getMonad("STATUS").bindValue(readWriteContext));

    //  Now do it again, only with the expected value for EVENT...
    readWriteContext.returnValue("EVENT", "StartEvent");
    ruleSystem.executeActivations();

    //  Verify that the rule system did change state this time:
    assertEqual("STARTED", readWriteContext.getMonad("STATUS").bindValue(readWriteContext));
  }

  @Test
  public void simplePerfTest() {
    //  Initialize rule system with its set of rules:
    CARuleSystem<String> ruleSystem = new CARuleSystem<String>(new StartEventRule());

    IReadWriteMonadex context = ruleSystem.freeVariables();

    //  Warm up the hotspot compiler...
    int iterations = 100000;
    for (int i = 0; i < iterations; i++) {
      if (i - ((i / 2) * 2) == i)
        context.returnValue("EVENT", "IdleEvent");
      else
        context.returnValue("EVENT", "StartEvent");
      ruleSystem.executeActivations();
    }

    //  Now make our measurement...
    iterations = 100000;
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      if (i - ((i / 2) * 2) == i)
        context.returnValue("EVENT", "IdleEvent");
      else
        context.returnValue("EVENT", "StartEvent");
      ruleSystem.executeActivations();
    }
    long elapsedTime = System.nanoTime() - startTime;
    System.out.println("One hundred thousand iterations, in nanos: " + elapsedTime);
    System.out.println("Average for one iteration, in nanos: " + (((double) elapsedTime) / ((double) iterations)));
  }

  @Test
  public void perfUsingMonadsTest() {
    //  Initialize rule system with its set of rules:
    CARuleSystem<String> ruleSystem = new CARuleSystem<String>(new StartEventRule());

    IReadWriteMonadex<IMonad<String>> context = ruleSystem.freeVariables();
    IMonad<String> event = context.getMonad("EVENT");


    //  Warm up the hotspot compiler...
    int iterations = 1000000;
    for (int i = 0; i < iterations; i++) {
      if (i - ((i / 2) * 2) == i)
        event.returnValue("IdleEvent", context);
      else
        event.returnValue("StartEvent", context);
      ruleSystem.executeActivations();
    }

    //  Now do the measurement...
    iterations = 100000;
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      if (i - ((i / 2) * 2) == i)
        event.returnValue("IdleEvent", context);
      else
        event.returnValue("StartEvent", context);
      ruleSystem.executeActivations();
    }
    long elapsedTime = System.nanoTime() - startTime;
    System.out.println("One hundred thousand iterations, in nanos: " + elapsedTime);
    System.out.println("Average for one iteration, in nanos: " + (((double) elapsedTime) / ((double) iterations)));
  }
}
