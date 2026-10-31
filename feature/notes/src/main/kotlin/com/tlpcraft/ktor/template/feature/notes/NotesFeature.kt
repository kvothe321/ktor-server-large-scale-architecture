package com.tlpcraft.ktor.template.feature.notes

import com.tlpcraft.ktor.template.core.server.ServerFeature
import com.tlpcraft.ktor.template.feature.notes.data.noteRepository
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.CreateNoteUseCase
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.DeleteNoteUseCase
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.GetNoteUseCase
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.ListNotesUseCase
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.UpdateNoteUseCase
import com.tlpcraft.ktor.template.feature.notes.presentation.NoteRoutes
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.routing.Route
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.ext.get

/** Local CRUD resource with pagination and validation. */
object NotesFeature : ServerFeature {
    override fun koinModule(config: ApplicationConfig): Module = module {
        single { noteRepository(database = get(), loggerFactory = get()) }
        singleOf(::CreateNoteUseCase)
        singleOf(::GetNoteUseCase)
        singleOf(::ListNotesUseCase)
        singleOf(::UpdateNoteUseCase)
        singleOf(::DeleteNoteUseCase)
        singleOf(::NoteRoutes)
    }

    override fun routes(route: Route) {
        route.get<NoteRoutes>().register(route)
    }
}
