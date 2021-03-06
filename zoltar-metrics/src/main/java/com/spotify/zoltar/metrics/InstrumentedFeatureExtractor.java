/*
 * Copyright (C) 2019 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spotify.zoltar.metrics;

import java.util.List;
import java.util.function.Function;

import com.spotify.zoltar.FeatureExtractor;
import com.spotify.zoltar.Model;
import com.spotify.zoltar.Vector;

/**
 * Instrumented Functional interface for feature extraction {@link FeatureExtractor}.
 *
 * @param <ModelT> underlying type of the {@link Model}.
 * @param <InputT> type of the input to feature extraction.
 * @param <ValueT> type of feature extraction result.
 */
@FunctionalInterface
interface InstrumentedFeatureExtractor<ModelT extends Model<?>, InputT, ValueT>
    extends FeatureExtractor<ModelT, InputT, ValueT> {

  /** Creates a new instrumented {@link FeatureExtractor}. */
  @SuppressWarnings("checkstyle:LineLength")
  static <ModelT extends Model<?>, InputT, ValueT>
      Function<
              FeatureExtractor<ModelT, InputT, ValueT>,
              InstrumentedFeatureExtractor<ModelT, InputT, ValueT>>
          create(final FeatureExtractorMetrics<InputT, ValueT> metrics) {
    return extractFn ->
        (model, inputs) -> {
          final VectorMetrics<InputT, ValueT> vectorMetrics = metrics.apply(model.id());
          final List<Vector<InputT, ValueT>> result = extractFn.extract(model, inputs);

          vectorMetrics.extraction(result);

          return result;
        };
  }
}
