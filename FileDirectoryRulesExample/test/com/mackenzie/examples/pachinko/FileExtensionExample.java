/*
   Example extensions of PACHINKO, A fast, embeddable micro-rule engine.
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

package com.mackenzie.examples.pachinko;

import com.mackenzieresearch.examples.pachinko.WatchingRuleSystem;
import com.mackenzieresearch.pachinko.DefaultCARule;
import com.mackenzieresearch.pachinko.Variable;
import com.mackenzieresearch.roux.kernel.IMonad;
import com.mackenzieresearch.roux.kernel.IMonadex;
import com.mackenzieresearch.roux.kernel.IReadOnlyMonad;
import com.mackenzieresearch.roux.kernel.IReadWriteMonadex;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static util.RuntimeAssertion.assertNotNull;
import static util.RuntimeAssertion.assertTrue;

public class FileExtensionExample {

  /**
   * This example REQUIRES JDK1.7 TO RUN.
   *
   * Simple rule that detects creation or modification of a file in the monitored director(ies) with a
   * specified file extension.  Signals detection of the file create/modify by putting the name of the
   * modified file in the RESULTS variable of the context.
   *
   * ...of course it could just as easily be monitoring for some more sophisticated condition and could
   * equally well perform some complex set of actions as a result.  This is just a simple example to
   * illustrate how the JDK7 WatchService mechanism can be integrated with Pachinko to allow you to
   * write condition-action rules that operate on changes to files in the file system.
   */
  public class FileExtRule extends DefaultCARule<WatchEvent> {
    String _channelName = null;
    String _fileExtent = null;
    int _event = -1;
    int _result = -1;

    public FileExtRule(String channelName, String fileExtent) {
      super();
      //  These are constants with respect to the rule's evaluation so they don't need to be declared
      //  as variables to the rule engine...
      assertNotNull(channelName);
      _channelName = channelName;
      _fileExtent = fileExtent;

      //  WatchEvent is a Variable (could also be a PKVariable) so that rule will react to its being set by
      //  evaluating the rule:
      _event = addVariable(new Variable<WatchEvent>(channelName, null));

      //  Result variable is optional so that rule will not depend on it being set before being evaluated:
      _result = addOptionalVariable(new Variable<String>("RESULT", null));
    }

    @Override
    public boolean evaluateCondition(IMonadex<? extends IReadOnlyMonad> context) {
      WatchEvent event = (WatchEvent) context.bindValue(_event);
      String filename = (String) event.context().toString();
      if (filename.endsWith(_fileExtent))
        return true;
      else
        return false;
    }

    @Override
    public void doAction(IReadWriteMonadex<? extends IMonad> context) {
      WatchEvent event = (WatchEvent) context.bindValue(_event);
      String filename = (String) event.context().toString();
      context.returnValue(_result, filename);
    }
  }


  //------------------------------------------------------------------------------------------------------------
  //
  //  Example usage of above file-extension monitoring rule to react to changes in the filesystem.  This
  //  test case simply looks for creation or modification of files with the file extension ".log", and then
  //  reports the name of the changed file.
  //
  //  A more interesting usage could easily be constructed where detection of files with certain file
  //  extensions causes the file to be processed into a stream of tokens (like, say, SAX parse events or
  //  the events resulting from traversing an Abstract Syntax Tree) and then the tokens passed to a second
  //  set of rules looking for interesting patterns in the contents of the file.  That case is currently
  //  left as an exercise for the reader, though at some point I'll be happy to share the rule-based
  //  AST processor I've built based on Pachinko, which I use to transform one AST into another by
  //  recursive application of simple rules.
  //
  @Test
  public void SimpleDirectoryMonitoringTest() throws IOException, InterruptedException {
    //  WatchingRuleSystem is an extension of Pachinko with convenience methods integrating it with
    //  the JDK 7 WatchService API...
    WatchingRuleSystem ruleSystem = new WatchingRuleSystem(new FileExtRule("logfile", ".log"))
            .addEventSource("logfile",
                    "/Users/corwyn/Development/pachinko/FileDirectoryRulesExample/watchableTestDir",
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

    //  Make sure we start out with no file in the test directory...
    File sampleFile = new File("/Users/corwyn/Development/pachinko/FileDirectoryRulesExample/watchableTestDir/test.log");
    sampleFile.delete();

    //  Now start monitoring the directory:
    ruleSystem.start();

    //  ..and create the test file which our sample rule should detect:
    sampleFile.createNewFile();

    //  Wait for extremely slow JDK7 implementation of WatchService to actually notice the file change...
    long startTime = System.currentTimeMillis();
    while ((System.currentTimeMillis() - startTime) < 60000) {
      if (ruleSystem.freeVariables().bindValue("RESULT") != null)
        break;
    }

    //  Now check to see if rule engine did its thing...
    ruleSystem.stop();
    String result = (String) ruleSystem.freeVariables().bindValue("RESULT");
    assertNotNull(result);
    assertTrue("test.log".equals(result));
  }
}
