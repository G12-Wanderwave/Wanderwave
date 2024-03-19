package ch.epfl.cs311.wanderwave;

import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = Wanderwave.class
)
@GeneratedEntryPoint
@InstallIn(SingletonComponent.class)
public interface Wanderwave_GeneratedInjector {
  void injectWanderwave(Wanderwave wanderwave);
}
