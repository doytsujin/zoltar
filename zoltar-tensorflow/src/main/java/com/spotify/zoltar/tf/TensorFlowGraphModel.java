/*-
 * -\-\-
 * zoltar-tensorflow
 * --
 * Copyright (C) 2016 - 2018 Spotify AB
 * --
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
 * -/-/-
 */

package com.spotify.zoltar.tf;

import com.google.auto.value.AutoValue;
import com.spotify.zoltar.Model;
import com.spotify.zoltar.fs.FileSystemExtras;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.framework.ConfigProto;

/**
 * This model can be used to load protobuf definition of a TensorFlow {@link Graph}.
 *
 * For an easy model freezing function see <a href="https://github.com/spotify/spotify-tensorflow/blob/master/spotify_tensorflow/freeze_graph.py">spotify-tensorflow</a>
 *
 * TensorFlowGraphModel is thread-safe.
 */
@AutoValue
public abstract class TensorFlowGraphModel implements Model<Session>, AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(TensorFlowGraphModel.class);

  /**
   * Note: Please use Models from zoltar-models module.
   *
   * Creates a TensorFlow model based on a frozen, serialized TensorFlow {@link Graph}.
   *
   * @param graphUri URI to the TensorFlow graph definition.
   * @param config config for TensorFlow {@link Session}.
   * @param prefix optional prefix that will be prepended to names in the graph.
   */
  public static TensorFlowGraphModel from(final URI graphUri,
                                          @Nullable final ConfigProto config,
                                          @Nullable final String prefix)
      throws IOException {
    final byte[] graphBytes = Files.readAllBytes(FileSystemExtras.path(graphUri));
    return from(graphBytes, config, prefix);
  }

  /**
   * Note: Please use Models from zoltar-models module.
   *
   * Creates a TensorFlow model based on a frozen, serialized TensorFlow {@link Graph}.
   *
   * @param graphDef byte array representing the TensorFlow {@link Graph} definition.
   * @param config ConfigProto config for TensorFlow {@link Session}.
   * @param prefix a prefix that will be prepended to names in graphDef.
   */
  public static TensorFlowGraphModel from(final byte[] graphDef,
                                          @Nullable final ConfigProto config,
                                          @Nullable final String prefix)
      throws IOException {
    final Graph graph = new Graph();
    final Session session = new Session(graph, config != null ? config.toByteArray() : null);
    final long loadStart = System.currentTimeMillis();
    if (prefix == null) {
      logger.debug("Loading graph definition without prefix");
      graph.importGraphDef(graphDef);
    } else {
      logger.debug("Loading graph definition with prefix: %s", prefix);
      graph.importGraphDef(graphDef, prefix);
    }
    logger.info("TensorFlow graph loaded in %d ms", System.currentTimeMillis() - loadStart);
    return new AutoValue_TensorFlowGraphModel(graph, session);
  }

  /** Close the model. */
  @Override
  public void close() {
    if (instance() != null) {
      logger.debug("Closing TensorFlow session");
      instance().close();
    }
    if (graph() != null) {
      logger.debug("Closing TensorFlow graph");
      graph().close();
    }
  }

  /** Returns TensorFlow graph. */
  public abstract Graph graph();

  /** Returns TensorFlow {@link Session}. */
  @Override
  public abstract Session instance();

}