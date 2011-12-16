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

import com.mackenzieresearch.pachinko.CAExpressionRule;
import com.mackenzieresearch.pachinko.CARuleSystem;
import com.mackenzieresearch.roux.context.Monadex;
import com.mackenzieresearch.roux.existential.Equals;
import com.mackenzieresearch.roux.kernel.IMonad;
import com.mackenzieresearch.roux.kernel.IReadOnlyMonad;
import com.mackenzieresearch.roux.kernel.IReadWriteMonadex;
import com.mackenzieresearch.roux.kernel.ISideEffectFreeAction;
import com.mackenzieresearch.roux.singleValued.reference.ContextRef;
import org.junit.Test;

import java.util.Map;

public class CAExpressionRulePerfTests {
  private class RuleEvaluatedTrue extends RuntimeException {
    private String _ruleName;

    public RuleEvaluatedTrue(String ruleName) {
      _ruleName = ruleName;
    }

    public String getRuleName() {
      return _ruleName;
    }
  }

  private class RuleEvaluatedFalse extends RuntimeException {
    private String _ruleName;

    public RuleEvaluatedFalse(String ruleName) {
      _ruleName = ruleName;
    }

    public String getRuleName() {
      return _ruleName;
    }
  }

  private class ThrowException implements ISideEffectFreeAction<Object> {
    private RuntimeException _exception = null;

    public ThrowException(RuntimeException exception) {
      _exception = exception;
    }

    @Override
    public void evaluate(IReadWriteMonadex<? extends IReadOnlyMonad<Object>> context) {
      throw _exception;
    }

    @Override
    public IReadWriteMonadex freeVariables() {
      return new Monadex(new IMonad[0]);
    }

    @Override
    public String[] freeVarNames() {
      return new String[0];
    }

    @Override
    public void setFreeVariables(Map<String, IMonad> freeVars) {
    }
  }

  private class NoOpAction implements ISideEffectFreeAction<Object> {

    @Override
    public void evaluate(IReadWriteMonadex<? extends IReadOnlyMonad<Object>> context) {
    }

    @Override
    public IReadWriteMonadex freeVariables() {
      return new Monadex(new IMonad[0]);
    }

    @Override
    public String[] freeVarNames() {
      return new String[0];
    }

    @Override
    public void setFreeVariables(Map<String, IMonad> freeVars) {
    }
  }


  @Test
  public void fiftyPercentDutyCyclePerfTest() {
    CAExpressionRule<Integer> rule = new CAExpressionRule()
            .whereCondition(new Equals()
                    .leftParam(new ContextRef<Integer>("sampleData"))
                    .rightParam(0))
            .performAction(new NoOpAction());

    CARuleSystem<Integer> ruleSystem = new CARuleSystem<Integer>(rule);

    IReadWriteMonadex context = ruleSystem.freeVariables();

    //  First warm up hotspot...
    int iterations = 100000;
    for (int i = 0; i < iterations; i++) {
      context.returnValue("sampleData", i - ((i / 2) * 2));
      ruleSystem.executeActivations();
    }

    //  Now make the actual measurement...
    iterations = 100000;
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      context.returnValue("sampleData", i - ((i / 2) * 2));
      ruleSystem.executeActivations();
    }
    long elapsedTime = System.nanoTime() - startTime;
    System.out.println("One hundred thousand iterations, in nanos: " + elapsedTime);
    System.out.println("Average for one iteration, in nanos: " + (((double) elapsedTime) / ((double) iterations)));
  }

  @Test
  public void usingCrefsPerfTest() {
    CAExpressionRule<Integer> rule = new CAExpressionRule()
            .whereCondition(new Equals()
                    .leftParam(new ContextRef<Integer>("sampleData"))
                    .rightParam(0))
            .performAction(new NoOpAction());

    CARuleSystem<Integer> ruleSystem = new CARuleSystem<Integer>(rule);

    IReadWriteMonadex context = ruleSystem.freeVariables();
    IMonad<Integer> sampleDataMonad = (IMonad<Integer>) context.getMonad("sampleData");

    //  First warm up hotspot...
    int iterations = 100000;
    for (int i = 0; i < iterations; i++) {
      sampleDataMonad.returnValue(i - ((i / 2) * 2), context);
      ruleSystem.executeActivations();
    }

    //  Now make the measurement...
    iterations = 100000;
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      sampleDataMonad.returnValue(i - ((i / 2) * 2), context);
      ruleSystem.executeActivations();
    }
    long elapsedTime = System.nanoTime() - startTime;
    System.out.println("One hundred thousand iterations, in nanos: " + elapsedTime);
    System.out.println("Average for one iteration, in nanos: " + (((double) elapsedTime) / ((double) iterations)));
  }

  @Test
  public void usingIndexedMonadsPerfTest() {
    CAExpressionRule<Integer> rule = new CAExpressionRule()
            .whereCondition(new Equals()
                    .leftParam(new ContextRef<Integer>("sampleData"))
                    .rightParam(0))
            .performAction(new NoOpAction());

    CARuleSystem<Integer> ruleSystem = new CARuleSystem<Integer>(rule);

    IReadWriteMonadex context = ruleSystem.freeVariables();
    int sampleDataMonadIndex = context.getIndex("sampleData");

    //  First warm up hotspot...
    int iterations = 100000;
    for (int i = 0; i < iterations; i++) {
      context.returnValue(sampleDataMonadIndex, i - ((i / 2) * 2));
      ruleSystem.executeActivations();
    }

    //  Now make the measurement...
    iterations = 100000;
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      context.returnValue(sampleDataMonadIndex, i - ((i / 2) * 2));
      ruleSystem.executeActivations();
    }
    long elapsedTime = System.nanoTime() - startTime;
    System.out.println("One hundred thousand iterations, in nanos: " + elapsedTime);
    System.out.println("Average for one iteration, in nanos: " + (((double) elapsedTime) / ((double) iterations)));
  }
}
