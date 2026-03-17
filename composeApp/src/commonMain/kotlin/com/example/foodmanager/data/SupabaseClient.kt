package com.example.foodmanager.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

/**
 * Supabase client singleton for the FoodManager app.
 * Postgrest and Auth are installed and ready to use.
 */
val supabase = createSupabaseClient(
    supabaseUrl = SupabaseConfig.projectUrl,
    supabaseKey = SupabaseConfig.publishableKey
) {
    install(Postgrest)
    install(Auth)
}
