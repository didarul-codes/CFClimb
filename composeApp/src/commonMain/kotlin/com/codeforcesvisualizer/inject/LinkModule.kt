package com.codeforcesvisualizer.inject

import com.codeforcesvisualizer.core.links.DeepLinks
import org.koin.dsl.module

val linkModule = module {
    single { DeepLinks() }
}
