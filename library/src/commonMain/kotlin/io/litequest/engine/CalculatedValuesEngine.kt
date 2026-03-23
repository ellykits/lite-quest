/*
* Copyright 2025 LiteQuest Contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.litequest.engine

import io.litequest.model.CalculatedValue
import io.litequest.util.DependencyExtractor

class CalculatedValuesEngine(
  private val evaluator: JsonLogicEvaluator,
  calculatedValues: List<CalculatedValue>,
) {
  private val dependencyGraph: Map<String, Set<String>> =
    calculatedValues.associate { calcValue ->
      calcValue.name to DependencyExtractor.extractDependencies(calcValue.expression)
    }

  private val sortedCalculatedValues: List<CalculatedValue> =
    sortTopologically(calculatedValues.associateBy { it.name })

  private fun sortTopologically(values: Map<String, CalculatedValue>): List<CalculatedValue> {
    val result = mutableListOf<CalculatedValue>()
    val visited = mutableSetOf<String>()
    val tempVisited = mutableSetOf<String>()

    fun visit(name: String) {
      // Simplified: ignore cycles for now
      if (tempVisited.contains(name)) return
      if (visited.contains(name)) return

      tempVisited.add(name)
      val dependencies = dependencyGraph[name] ?: emptySet()
      dependencies.forEach { dep -> if (values.containsKey(dep)) visit(dep) }
      tempVisited.remove(name)
      visited.add(name)
      values[name]?.let { result.add(it) }
    }

    values.keys.forEach { visit(it) }
    return result
  }

  fun evaluate(dataContext: MutableMap<String, Any?>): Map<String, Any?> {
    val results = mutableMapOf<String, Any?>()
    sortedCalculatedValues.forEach { calcValue ->
      val result = evaluator.evaluate(calcValue.expression, dataContext)
      results[calcValue.name] = result
      dataContext[calcValue.name] = result
    }
    return results
  }

  fun evaluateIncremental(
    dataContext: MutableMap<String, Any?>,
    changedFields: Set<String>,
  ): Map<String, Any?> {
    val affectedNames = mutableSetOf<String>()

    fun findAffected(name: String) {
      if (affectedNames.contains(name)) return
      affectedNames.add(name)
      // find calculations that depend on this one
      dependencyGraph.forEach { (calcName, deps) -> if (name in deps) findAffected(calcName) }
    }

    changedFields.forEach { field ->
      dependencyGraph.forEach { (calcName, deps) -> if (field in deps) findAffected(calcName) }
    }

    val results = mutableMapOf<String, Any?>()
    // Evaluate only affected ones, BUT in topological order
    sortedCalculatedValues.forEach { calcValue ->
      if (calcValue.name in affectedNames) {
        val result = evaluator.evaluate(calcValue.expression, dataContext)
        results[calcValue.name] = result
        dataContext[calcValue.name] = result
      }
    }
    return results
  }
}
