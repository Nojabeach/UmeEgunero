package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
 
@HiltViewModel
class GestionUsuariosViewModel @Inject constructor() : ViewModel() {
    // No necesitamos mantener ningún estado ya que eliminamos la funcionalidad de los 5 clicks
} 