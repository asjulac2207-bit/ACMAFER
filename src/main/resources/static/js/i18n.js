/* ACMAFER — Internacionalización (i18n) v2.0 */
'use strict';

(function () {

  /* ── Traducciones ──────────────────────────────────────────────────── */
  const translations = {
    es: {
      /* Navbar */
      nav_dashboard:       'Dashboard',
      nav_catalogo:        'Catálogo',
      nav_pedidos:         'Pedidos',
      nav_todos_pedidos:   'Todos los pedidos',
      nav_mis_pedidos:     'Mis pedidos',
      nav_nuevo_pedido:    'Nuevo pedido',
      nav_tareas:          'Tareas',
      nav_ver_tareas:      'Ver tareas',
      nav_nueva_tarea:     'Nueva tarea',
      nav_reportes:        'Reportes',
      nav_usuarios:        'Usuarios',
      nav_mi_perfil:       'Mi perfil',
      nav_cerrar_sesion:   'Cerrar sesión',
      nav_ingresar:        'Ingresar',
      nav_registrarse:     'Registrarse',
      theme_title:         'Cambiar tema',
      notif_title:         'Notificaciones',
      /* Chatbot */
      chat_greeting:       'Hola 👋 Soy <b>AcmaBot</b>. Puedo ayudarte con inventario, pedidos y más.',
      chat_placeholder:    'Escribe tu pregunta...',
      /* Auth - Login */
      login_title:         'Sistema de Gestión Industrial',
      login_email_label:   'Correo electrónico',
      login_email_ph:      'correo@acmafer.com',
      login_pass_label:    'Contraseña',
      login_btn:           '🔥 Ingresar al Sistema',
      login_google:        'Continuar con Google',
      login_no_account:    '¿No tienes cuenta?',
      login_register_link: 'Regístrate aquí',
      login_forgot:        '¿Olvidaste tu contraseña?',
      login_skip:          'Saltar intro ›',
      /* Auth - Registro */
      reg_title:           'Crear cuenta',
      reg_subtitle:        'Únete al sistema de gestión ACMAFER',
      reg_name:            'Nombre completo',
      reg_name_ph:         'Tu nombre completo',
      reg_email:           'Correo electrónico',
      reg_email_ph:        'correo@ejemplo.com',
      reg_pass:            'Contraseña',
      reg_pass_ph:         'Mínimo 8 caracteres',
      reg_confirm:         'Confirmar contraseña',
      reg_confirm_ph:      'Repite la contraseña',
      reg_btn:             'Crear cuenta',
      reg_have_account:    '¿Ya tienes cuenta?',
      reg_login_link:      'Iniciar sesión',
      /* Auth - Recuperar */
      rec_title:           'Recuperar contraseña',
      rec_subtitle:        'Ingresa tu correo y te enviaremos un código de verificación.',
      rec_email_label:     'Correo registrado',
      rec_btn:             'Enviar código',
      rec_back:            'Volver al login',
      /* Auth - Verificar */
      ver_title:           'Verificar código',
      ver_subtitle:        'Ingresa el código de 6 dígitos enviado a tu correo.',
      ver_code_label:      'Código de verificación',
      ver_code_ph:         '000000',
      ver_btn:             'Verificar',
      ver_resend:          'Reenviar código',
      ver_back:            'Volver',
      /* Dashboard común */
      dash_welcome:        'Bienvenido',
      dash_my_panel:       'Mi Panel',
      dash_exec:           'Dashboard Ejecutivo',
      dash_admin_sub:      'Panel de control — Administrador',
      dash_sup_title:      'Panel de Supervisión',
      dash_sup_sub:        'Control operativo del equipo y pedidos',
      /* KPI labels */
      kpi_active_tasks:    'Tareas activas',
      kpi_completed_tasks: 'Tareas completadas',
      kpi_my_orders:       'Mis pedidos',
      kpi_pending_orders:  'Pedidos pendientes',
      kpi_products:        'Productos',
      kpi_users:           'Usuarios',
      kpi_revenue:         'Ingresos',
      kpi_performance:     'Rendimiento',
      /* Nav dashboard standalone */
      dash_nav_dashboard:  'Dashboard',
      dash_nav_catalogo:   'Catálogo',
      dash_nav_pedidos:    'Pedidos',
      dash_nav_mis_pedidos:'Mis pedidos',
      dash_nav_tareas:     'Tareas',
      dash_nav_mis_tareas: 'Mis tareas',
      dash_nav_rendimiento:'Rendimiento',
      dash_nav_mi_rend:    'Mi rendimiento',
      dash_nav_reportes:   'Reportes',
      dash_nav_perfil:     'Mi perfil',
      dash_nav_logout:     'Cerrar sesión',
      /* Catálogo */
      cat_title:           'Catálogo de Productos',
      cat_search_ph:       'Buscar producto...',
      cat_all:             'Todos',
      cat_add_cart:        'Agregar',
      cat_no_stock:        'Sin stock',
      cat_view:            'Ver detalle',
      cat_filter:          'Filtrar',
      cat_search_btn:      'Buscar',
      /* Pedidos */
      ped_title:           'Gestión de Pedidos',
      ped_search_ph:       'N° pedido, nombre de solicitante...',
      ped_search_label:    'Buscar',
      ped_status_label:    'Estado',
      ped_all:             'Todos',
      ped_filter_btn:      'Filtrar',
      ped_clear_btn:       'Limpiar',
      ped_my_title:        'Mis Pedidos',
      /* Tareas */
      tar_title:           'Gestión de Tareas',
      tar_my_title:        'Mis Tareas',
      tar_new_btn:         'Nueva tarea',
      tar_search_ph:       'Buscar tarea...',
      tar_status_all:      'Todos los estados',
      tar_filter_btn:      'Filtrar',
      /* Usuarios */
      usr_title:           'Gestión de Usuarios',
      usr_new_btn:         'Nuevo usuario',
      usr_search_ph:       'Buscar usuario...',
      /* Perfil */
      prf_title:           'Mi Perfil',
      prf_edit_btn:        'Editar perfil',
      prf_change_pass:     'Cambiar contraseña',
      prf_save_btn:        'Guardar cambios',
      /* Reportes */
      rep_title:           'Reportes',
      rep_generate:        'Generar reporte',
      /* Notificaciones */
      notif_page_title:    'Notificaciones',
      notif_mark_all:      'Marcar todo como leído',
      /* Errores */
      err_404_title:       'Página no encontrada',
      err_404_msg:         'La página que buscas no existe.',
      err_500_title:       'Error del servidor',
      err_500_msg:         'Algo salió mal. Inténtalo de nuevo.',
      err_403_title:       'Acceso denegado',
      err_403_msg:         'No tienes permiso para ver esta página.',
      err_back_btn:        'Volver al inicio',
      /* Comunes */
      common_save:         'Guardar',
      common_cancel:       'Cancelar',
      common_delete:       'Eliminar',
      common_edit:         'Editar',
      common_back:         'Volver',
      common_yes:          'Sí',
      common_no:           'No',
      common_search:       'Buscar',
      common_filter:       'Filtrar',
      common_clear:        'Limpiar',
      common_loading:      'Cargando...',
      common_actions:      'Acciones',
      common_state:        'Estado',
      common_date:         'Fecha',
      common_name:         'Nombre',
      common_email:        'Correo',
      ped_list_title:           'Lista de pedidos',
      ped_col_num:           'N° Pedido',
      ped_empty:           'No tienes pedidos aún.',
      common_products:           'Productos',
      common_total:           'Total',
      cat_found:           'productos encontrados',
      cat_all_states:           'Todos los estados',
      cat_no_results:           'No se encontraron productos con esos criterios.',
      cat_view_all:           'Ver todos',
      dash_subtitle:           'Bienvenido al sistema de gestión ACMAFER. Aquí está el resumen de hoy.',
      kpi_orders:           'Pedidos',
      kpi_available:           'disponibles',
      kpi_pending:           'pendientes',
      kpi_low_stock:           'Stock Bajo',
      kpi_to_attend:           'Por Atender',
      dash_top_products:           'PRODUCTOS MÁS VENDIDOS',
      dash_sold:           'vendidos',
      dash_orders_by_state:           'Pedidos por Estado',
      dash_products_by_cat:           'Productos por Categoría',
      dash_critical_stock:           'Stock Crítico',
      dash_full_inventory:           'Ver inventario completo',
      dash_btn_catalog:           'Ver Catálogo',
      dash_export_pdf:           'Exportar PDF',
      dash_new_product:           'Nuevo Producto',
      /* Selector de idioma */
      lang_selector_title: 'Idioma',
    },
    en: {
      nav_dashboard:       'Dashboard',
      nav_catalogo:        'Catalogue',
      nav_pedidos:         'Orders',
      nav_todos_pedidos:   'All orders',
      nav_mis_pedidos:     'My orders',
      nav_nuevo_pedido:    'New order',
      nav_tareas:          'Tasks',
      nav_ver_tareas:      'View tasks',
      nav_nueva_tarea:     'New task',
      nav_reportes:        'Reports',
      nav_usuarios:        'Users',
      nav_mi_perfil:       'My profile',
      nav_cerrar_sesion:   'Sign out',
      nav_ingresar:        'Log in',
      nav_registrarse:     'Register',
      theme_title:         'Toggle theme',
      notif_title:         'Notifications',
      chat_greeting:       'Hi 👋 I\'m <b>AcmaBot</b>. I can help you with inventory, orders and more.',
      chat_placeholder:    'Type your question...',
      login_title:         'Industrial Management System',
      login_email_label:   'Email address',
      login_email_ph:      'mail@acmafer.com',
      login_pass_label:    'Password',
      login_btn:           '🔥 Log in to System',
      login_google:        'Continue with Google',
      login_no_account:    'Don\'t have an account?',
      login_register_link: 'Register here',
      login_forgot:        'Forgot your password?',
      login_skip:          'Skip intro ›',
      reg_title:           'Create account',
      reg_subtitle:        'Join the ACMAFER management system',
      reg_name:            'Full name',
      reg_name_ph:         'Your full name',
      reg_email:           'Email address',
      reg_email_ph:        'mail@example.com',
      reg_pass:            'Password',
      reg_pass_ph:         'At least 8 characters',
      reg_confirm:         'Confirm password',
      reg_confirm_ph:      'Repeat password',
      reg_btn:             'Create account',
      reg_have_account:    'Already have an account?',
      reg_login_link:      'Log in',
      rec_title:           'Reset password',
      rec_subtitle:        'Enter your email and we\'ll send you a verification code.',
      rec_email_label:     'Registered email',
      rec_btn:             'Send code',
      rec_back:            'Back to login',
      ver_title:           'Verify code',
      ver_subtitle:        'Enter the 6-digit code sent to your email.',
      ver_code_label:      'Verification code',
      ver_code_ph:         '000000',
      ver_btn:             'Verify',
      ver_resend:          'Resend code',
      ver_back:            'Back',
      dash_welcome:        'Welcome',
      dash_my_panel:       'My Panel',
      dash_exec:           'Executive Dashboard',
      dash_admin_sub:      'Control panel — Administrator',
      dash_sup_title:      'Supervision Panel',
      dash_sup_sub:        'Operational control of team and orders',
      kpi_active_tasks:    'Active tasks',
      kpi_completed_tasks: 'Completed tasks',
      kpi_my_orders:       'My orders',
      kpi_pending_orders:  'Pending orders',
      kpi_products:        'Products',
      kpi_users:           'Users',
      kpi_revenue:         'Revenue',
      kpi_performance:     'Performance',
      dash_nav_dashboard:  'Dashboard',
      dash_nav_catalogo:   'Catalogue',
      dash_nav_pedidos:    'Orders',
      dash_nav_mis_pedidos:'My orders',
      dash_nav_tareas:     'Tasks',
      dash_nav_mis_tareas: 'My tasks',
      dash_nav_rendimiento:'Performance',
      dash_nav_mi_rend:    'My performance',
      dash_nav_reportes:   'Reports',
      dash_nav_perfil:     'My profile',
      dash_nav_logout:     'Sign out',
      cat_title:           'Product Catalogue',
      cat_search_ph:       'Search product...',
      cat_all:             'All',
      cat_add_cart:        'Add',
      cat_no_stock:        'Out of stock',
      cat_view:            'View detail',
      cat_filter:          'Filter',
      cat_search_btn:      'Search',
      ped_title:           'Order Management',
      ped_search_ph:       'Order #, requester name...',
      ped_search_label:    'Search',
      ped_status_label:    'Status',
      ped_all:             'All',
      ped_filter_btn:      'Filter',
      ped_clear_btn:       'Clear',
      ped_my_title:        'My Orders',
      tar_title:           'Task Management',
      tar_my_title:        'My Tasks',
      tar_new_btn:         'New task',
      tar_search_ph:       'Search task...',
      tar_status_all:      'All statuses',
      tar_filter_btn:      'Filter',
      usr_title:           'User Management',
      usr_new_btn:         'New user',
      usr_search_ph:       'Search user...',
      prf_title:           'My Profile',
      prf_edit_btn:        'Edit profile',
      prf_change_pass:     'Change password',
      prf_save_btn:        'Save changes',
      rep_title:           'Reports',
      rep_generate:        'Generate report',
      notif_page_title:    'Notifications',
      notif_mark_all:      'Mark all as read',
      err_404_title:       'Page not found',
      err_404_msg:         'The page you are looking for does not exist.',
      err_500_title:       'Server error',
      err_500_msg:         'Something went wrong. Please try again.',
      err_403_title:       'Access denied',
      err_403_msg:         'You do not have permission to view this page.',
      err_back_btn:        'Back to home',
      common_save:         'Save',
      common_cancel:       'Cancel',
      common_delete:       'Delete',
      common_edit:         'Edit',
      common_back:         'Back',
      common_yes:          'Yes',
      common_no:           'No',
      common_search:       'Search',
      common_filter:       'Filter',
      common_clear:        'Clear',
      common_loading:      'Loading...',
      common_actions:      'Actions',
      common_state:        'Status',
      common_date:         'Date',
      common_name:         'Name',
      common_email:        'Email',
      ped_list_title:           'Order list',
      ped_col_num:           'Order #',
      ped_empty:           'You have no orders yet.',
      common_products:           'Products',
      common_total:           'Total',
      cat_found:           'products found',
      cat_all_states:           'All statuses',
      cat_no_results:           'No products found matching those criteria.',
      cat_view_all:           'View all',
      dash_subtitle:           'Welcome to the ACMAFER management system. Here is today\'s summary.',
      kpi_orders:           'Orders',
      kpi_available:           'available',
      kpi_pending:           'pending',
      kpi_low_stock:           'Low Stock',
      kpi_to_attend:           'To Attend',
      dash_top_products:           'TOP SELLING PRODUCTS',
      dash_sold:           'sold',
      dash_orders_by_state:           'Orders by Status',
      dash_products_by_cat:           'Products by Category',
      dash_critical_stock:           'Critical Stock',
      dash_full_inventory:           'View full inventory',
      dash_btn_catalog:           'View Catalogue',
      dash_export_pdf:           'Export PDF',
      dash_new_product:           'New Product',
      lang_selector_title: 'Language',
    },
    it: {
      nav_dashboard:       'Dashboard',
      nav_catalogo:        'Catalogo',
      nav_pedidos:         'Ordini',
      nav_todos_pedidos:   'Tutti gli ordini',
      nav_mis_pedidos:     'I miei ordini',
      nav_nuevo_pedido:    'Nuovo ordine',
      nav_tareas:          'Attività',
      nav_ver_tareas:      'Vedi attività',
      nav_nueva_tarea:     'Nuova attività',
      nav_reportes:        'Report',
      nav_usuarios:        'Utenti',
      nav_mi_perfil:       'Il mio profilo',
      nav_cerrar_sesion:   'Disconnetti',
      nav_ingresar:        'Accedi',
      nav_registrarse:     'Registrati',
      theme_title:         'Cambia tema',
      notif_title:         'Notifiche',
      chat_greeting:       'Ciao 👋 Sono <b>AcmaBot</b>. Posso aiutarti con inventario, ordini e altro.',
      chat_placeholder:    'Scrivi la tua domanda...',
      login_title:         'Sistema di Gestione Industriale',
      login_email_label:   'Indirizzo email',
      login_email_ph:      'email@acmafer.com',
      login_pass_label:    'Password',
      login_btn:           '🔥 Accedi al Sistema',
      login_google:        'Continua con Google',
      login_no_account:    'Non hai un account?',
      login_register_link: 'Registrati qui',
      login_forgot:        'Hai dimenticato la password?',
      login_skip:          'Salta intro ›',
      reg_title:           'Crea account',
      reg_subtitle:        'Unisciti al sistema di gestione ACMAFER',
      reg_name:            'Nome completo',
      reg_name_ph:         'Il tuo nome completo',
      reg_email:           'Indirizzo email',
      reg_email_ph:        'email@esempio.com',
      reg_pass:            'Password',
      reg_pass_ph:         'Almeno 8 caratteri',
      reg_confirm:         'Conferma password',
      reg_confirm_ph:      'Ripeti la password',
      reg_btn:             'Crea account',
      reg_have_account:    'Hai già un account?',
      reg_login_link:      'Accedi',
      rec_title:           'Recupera password',
      rec_subtitle:        'Inserisci la tua email e ti invieremo un codice di verifica.',
      rec_email_label:     'Email registrata',
      rec_btn:             'Invia codice',
      rec_back:            'Torna al login',
      ver_title:           'Verifica codice',
      ver_subtitle:        'Inserisci il codice a 6 cifre inviato alla tua email.',
      ver_code_label:      'Codice di verifica',
      ver_code_ph:         '000000',
      ver_btn:             'Verifica',
      ver_resend:          'Invia di nuovo',
      ver_back:            'Indietro',
      dash_welcome:        'Benvenuto',
      dash_my_panel:       'Il mio pannello',
      dash_exec:           'Dashboard Esecutiva',
      dash_admin_sub:      'Pannello di controllo — Amministratore',
      dash_sup_title:      'Pannello di Supervisione',
      dash_sup_sub:        'Controllo operativo del team e degli ordini',
      kpi_active_tasks:    'Attività attive',
      kpi_completed_tasks: 'Attività completate',
      kpi_my_orders:       'I miei ordini',
      kpi_pending_orders:  'Ordini in sospeso',
      kpi_products:        'Prodotti',
      kpi_users:           'Utenti',
      kpi_revenue:         'Entrate',
      kpi_performance:     'Prestazioni',
      dash_nav_dashboard:  'Dashboard',
      dash_nav_catalogo:   'Catalogo',
      dash_nav_pedidos:    'Ordini',
      dash_nav_mis_pedidos:'I miei ordini',
      dash_nav_tareas:     'Attività',
      dash_nav_mis_tareas: 'Le mie attività',
      dash_nav_rendimiento:'Prestazioni',
      dash_nav_mi_rend:    'Le mie prestazioni',
      dash_nav_reportes:   'Report',
      dash_nav_perfil:     'Il mio profilo',
      dash_nav_logout:     'Disconnetti',
      cat_title:           'Catalogo Prodotti',
      cat_search_ph:       'Cerca prodotto...',
      cat_all:             'Tutti',
      cat_add_cart:        'Aggiungi',
      cat_no_stock:        'Esaurito',
      cat_view:            'Vedi dettaglio',
      cat_filter:          'Filtra',
      cat_search_btn:      'Cerca',
      ped_title:           'Gestione Ordini',
      ped_search_ph:       'N° ordine, nome richiedente...',
      ped_search_label:    'Cerca',
      ped_status_label:    'Stato',
      ped_all:             'Tutti',
      ped_filter_btn:      'Filtra',
      ped_clear_btn:       'Cancella',
      ped_my_title:        'I miei Ordini',
      tar_title:           'Gestione Attività',
      tar_my_title:        'Le mie Attività',
      tar_new_btn:         'Nuova attività',
      tar_search_ph:       'Cerca attività...',
      tar_status_all:      'Tutti gli stati',
      tar_filter_btn:      'Filtra',
      usr_title:           'Gestione Utenti',
      usr_new_btn:         'Nuovo utente',
      usr_search_ph:       'Cerca utente...',
      prf_title:           'Il mio Profilo',
      prf_edit_btn:        'Modifica profilo',
      prf_change_pass:     'Cambia password',
      prf_save_btn:        'Salva modifiche',
      rep_title:           'Report',
      rep_generate:        'Genera report',
      notif_page_title:    'Notifiche',
      notif_mark_all:      'Segna tutto come letto',
      err_404_title:       'Pagina non trovata',
      err_404_msg:         'La pagina che cerchi non esiste.',
      err_500_title:       'Errore del server',
      err_500_msg:         'Qualcosa è andato storto. Riprova.',
      err_403_title:       'Accesso negato',
      err_403_msg:         'Non hai il permesso di visualizzare questa pagina.',
      err_back_btn:        'Torna alla home',
      common_save:         'Salva',
      common_cancel:       'Annulla',
      common_delete:       'Elimina',
      common_edit:         'Modifica',
      common_back:         'Indietro',
      common_yes:          'Sì',
      common_no:           'No',
      common_search:       'Cerca',
      common_filter:       'Filtra',
      common_clear:        'Cancella',
      common_loading:      'Caricamento...',
      common_actions:      'Azioni',
      common_state:        'Stato',
      common_date:         'Data',
      common_name:         'Nome',
      common_email:        'Email',
      ped_list_title:           'Lista ordini',
      ped_col_num:           'N° Ordine',
      ped_empty:           'Non hai ancora ordini.',
      common_products:           'Prodotti',
      common_total:           'Totale',
      cat_found:           'prodotti trovati',
      cat_all_states:           'Tutti gli stati',
      cat_no_results:           'Nessun prodotto trovato con questi criteri.',
      cat_view_all:           'Vedi tutti',
      dash_subtitle:           'Benvenuto nel sistema di gestione ACMAFER. Ecco il riepilogo di oggi.',
      kpi_orders:           'Ordini',
      kpi_available:           'disponibili',
      kpi_pending:           'in sospeso',
      kpi_low_stock:           'Stock Basso',
      kpi_to_attend:           'Da Gestire',
      dash_top_products:           'PRODOTTI PIÙ VENDUTI',
      dash_sold:           'venduti',
      dash_orders_by_state:           'Ordini per Stato',
      dash_products_by_cat:           'Prodotti per Categoria',
      dash_critical_stock:           'Stock Critico',
      dash_full_inventory:           'Vedi inventario completo',
      dash_btn_catalog:           'Vedi Catalogo',
      dash_export_pdf:           'Esporta PDF',
      dash_new_product:           'Nuovo Prodotto',
      lang_selector_title: 'Lingua',
    },
    fr: {
      nav_dashboard:       'Tableau de bord',
      nav_catalogo:        'Catalogue',
      nav_pedidos:         'Commandes',
      nav_todos_pedidos:   'Toutes les commandes',
      nav_mis_pedidos:     'Mes commandes',
      nav_nuevo_pedido:    'Nouvelle commande',
      nav_tareas:          'Tâches',
      nav_ver_tareas:      'Voir les tâches',
      nav_nueva_tarea:     'Nouvelle tâche',
      nav_reportes:        'Rapports',
      nav_usuarios:        'Utilisateurs',
      nav_mi_perfil:       'Mon profil',
      nav_cerrar_sesion:   'Se déconnecter',
      nav_ingresar:        'Connexion',
      nav_registrarse:     "S'inscrire",
      theme_title:         'Changer le thème',
      notif_title:         'Notifications',
      chat_greeting:       "Bonjour 👋 Je suis <b>AcmaBot</b>. Je peux vous aider avec l'inventaire, les commandes et plus.",
      chat_placeholder:    'Écrivez votre question...',
      login_title:         'Système de Gestion Industrielle',
      login_email_label:   'Adresse e-mail',
      login_email_ph:      'email@acmafer.com',
      login_pass_label:    'Mot de passe',
      login_btn:           '🔥 Accéder au Système',
      login_google:        'Continuer avec Google',
      login_no_account:    "Vous n'avez pas de compte ?",
      login_register_link: 'Inscrivez-vous ici',
      login_forgot:        'Mot de passe oublié ?',
      login_skip:          "Passer l'intro ›",
      reg_title:           'Créer un compte',
      reg_subtitle:        'Rejoignez le système de gestion ACMAFER',
      reg_name:            'Nom complet',
      reg_name_ph:         'Votre nom complet',
      reg_email:           'Adresse e-mail',
      reg_email_ph:        'email@exemple.com',
      reg_pass:            'Mot de passe',
      reg_pass_ph:         'Au moins 8 caractères',
      reg_confirm:         'Confirmer le mot de passe',
      reg_confirm_ph:      'Répétez le mot de passe',
      reg_btn:             'Créer un compte',
      reg_have_account:    'Vous avez déjà un compte ?',
      reg_login_link:      'Se connecter',
      rec_title:           'Récupérer le mot de passe',
      rec_subtitle:        'Entrez votre e-mail et nous vous enverrons un code de vérification.',
      rec_email_label:     'E-mail enregistré',
      rec_btn:             'Envoyer le code',
      rec_back:            'Retour à la connexion',
      ver_title:           'Vérifier le code',
      ver_subtitle:        'Entrez le code à 6 chiffres envoyé à votre e-mail.',
      ver_code_label:      'Code de vérification',
      ver_code_ph:         '000000',
      ver_btn:             'Vérifier',
      ver_resend:          'Renvoyer le code',
      ver_back:            'Retour',
      dash_welcome:        'Bienvenue',
      dash_my_panel:       'Mon panneau',
      dash_exec:           'Tableau de bord exécutif',
      dash_admin_sub:      'Panneau de contrôle — Administrateur',
      dash_sup_title:      'Panneau de supervision',
      dash_sup_sub:        "Contrôle opérationnel de l'équipe et des commandes",
      kpi_active_tasks:    'Tâches actives',
      kpi_completed_tasks: 'Tâches terminées',
      kpi_my_orders:       'Mes commandes',
      kpi_pending_orders:  'Commandes en attente',
      kpi_products:        'Produits',
      kpi_users:           'Utilisateurs',
      kpi_revenue:         'Revenus',
      kpi_performance:     'Performance',
      dash_nav_dashboard:  'Tableau de bord',
      dash_nav_catalogo:   'Catalogue',
      dash_nav_pedidos:    'Commandes',
      dash_nav_mis_pedidos:'Mes commandes',
      dash_nav_tareas:     'Tâches',
      dash_nav_mis_tareas: 'Mes tâches',
      dash_nav_rendimiento:'Performance',
      dash_nav_mi_rend:    'Ma performance',
      dash_nav_reportes:   'Rapports',
      dash_nav_perfil:     'Mon profil',
      dash_nav_logout:     'Se déconnecter',
      cat_title:           'Catalogue de Produits',
      cat_search_ph:       'Rechercher un produit...',
      cat_all:             'Tous',
      cat_add_cart:        'Ajouter',
      cat_no_stock:        'En rupture de stock',
      cat_view:            'Voir le détail',
      cat_filter:          'Filtrer',
      cat_search_btn:      'Rechercher',
      ped_title:           'Gestion des Commandes',
      ped_search_ph:       'N° commande, nom du demandeur...',
      ped_search_label:    'Rechercher',
      ped_status_label:    'Statut',
      ped_all:             'Tous',
      ped_filter_btn:      'Filtrer',
      ped_clear_btn:       'Effacer',
      ped_my_title:        'Mes Commandes',
      tar_title:           'Gestion des Tâches',
      tar_my_title:        'Mes Tâches',
      tar_new_btn:         'Nouvelle tâche',
      tar_search_ph:       'Rechercher une tâche...',
      tar_status_all:      'Tous les statuts',
      tar_filter_btn:      'Filtrer',
      usr_title:           'Gestion des Utilisateurs',
      usr_new_btn:         'Nouvel utilisateur',
      usr_search_ph:       'Rechercher un utilisateur...',
      prf_title:           'Mon Profil',
      prf_edit_btn:        'Modifier le profil',
      prf_change_pass:     'Changer le mot de passe',
      prf_save_btn:        'Enregistrer les modifications',
      rep_title:           'Rapports',
      rep_generate:        'Générer un rapport',
      notif_page_title:    'Notifications',
      notif_mark_all:      'Tout marquer comme lu',
      err_404_title:       'Page non trouvée',
      err_404_msg:         "La page que vous recherchez n'existe pas.",
      err_500_title:       'Erreur serveur',
      err_500_msg:         "Quelque chose s'est mal passé. Veuillez réessayer.",
      err_403_title:       'Accès refusé',
      err_403_msg:         "Vous n'avez pas la permission de voir cette page.",
      err_back_btn:        "Retour à l'accueil",
      common_save:         'Enregistrer',
      common_cancel:       'Annuler',
      common_delete:       'Supprimer',
      common_edit:         'Modifier',
      common_back:         'Retour',
      common_yes:          'Oui',
      common_no:           'Non',
      common_search:       'Rechercher',
      common_filter:       'Filtrer',
      common_clear:        'Effacer',
      common_loading:      'Chargement...',
      common_actions:      'Actions',
      common_state:        'Statut',
      common_date:         'Date',
      common_name:         'Nom',
      common_email:        'E-mail',
      ped_list_title:           'Liste des commandes',
      ped_col_num:           'N° Commande',
      ped_empty:           'Vous n\'avez pas encore de commandes.',
      common_products:           'Produits',
      common_total:           'Total',
      cat_found:           'produits trouvés',
      cat_all_states:           'Tous les statuts',
      cat_no_results:           'Aucun produit trouvé avec ces critères.',
      cat_view_all:           'Voir tous',
      dash_subtitle:           'Bienvenue dans le système de gestion ACMAFER. Voici le résumé du jour.',
      kpi_orders:           'Commandes',
      kpi_available:           'disponibles',
      kpi_pending:           'en attente',
      kpi_low_stock:           'Stock Faible',
      kpi_to_attend:           'À Traiter',
      dash_top_products:           'PRODUITS LES PLUS VENDUS',
      dash_sold:           'vendus',
      dash_orders_by_state:           'Commandes par Statut',
      dash_products_by_cat:           'Produits par Catégorie',
      dash_critical_stock:           'Stock Critique',
      dash_full_inventory:           'Voir inventaire complet',
      dash_btn_catalog:           'Voir le Catalogue',
      dash_export_pdf:           'Exporter PDF',
      dash_new_product:           'Nouveau Produit',
      lang_selector_title: 'Langue',
    }
  };

  const LANGS = ['es', 'en', 'it', 'fr'];
  const FLAGS = { es: '🇪🇸', en: '🇬🇧', it: '🇮🇹', fr: '🇫🇷' };
  const NAMES = { es: 'Español', en: 'English', it: 'Italiano', fr: 'Français' };
  const CODES = { es: 'ES', en: 'EN', it: 'IT', fr: 'FR' };

  /* ── Aplicar traducciones al DOM ───────────────────────────────────── */
  function applyLang(lang) {
    const t = translations[lang] || translations['es'];

    document.querySelectorAll('[data-i18n]').forEach(el => {
      const key = el.dataset.i18n;
      if (t[key] !== undefined) {
        const icon = el.querySelector('i.bi');
        if (icon) {
          el.innerHTML = '';
          el.appendChild(icon);
          el.appendChild(document.createTextNode(' ' + t[key]));
        } else if (el.dataset.i18nHtml) {
          el.innerHTML = t[key];
        } else {
          el.textContent = t[key];
        }
      }
    });

    document.querySelectorAll('[data-i18n-title]').forEach(el => {
      const key = el.dataset.i18nTitle;
      if (t[key] !== undefined) el.title = t[key];
    });

    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
      const key = el.dataset.i18nPlaceholder;
      if (t[key] !== undefined) el.placeholder = t[key];
    });

    document.documentElement.lang = lang;

    /* Marcar opción activa dentro de cada dropdown de idioma */
    document.querySelectorAll('.lang-option').forEach(btn => {
      btn.classList.toggle('lang-active', btn.dataset.lang === lang);
    });

    /* Actualizar el código mostrado en el botón disparador (ej: "ES") */
    document.querySelectorAll('.lang-trigger-code').forEach(el => {
      el.textContent = CODES[lang];
    });
  }

  /* ── Cambiar idioma ────────────────────────────────────────────────── */
  window.acmSetLang = function (lang) {
    if (!LANGS.includes(lang)) return;
    localStorage.setItem('acmafer_lang', lang);
    applyLang(lang);
    /* cerrar cualquier dropdown abierto tras seleccionar */
    document.querySelectorAll('.lang-switcher.lang-open').forEach(c => c.classList.remove('lang-open'));
  };

  /* ── Renderizar el selector tipo dropdown (🌐 + menú) ───────────────
     Reemplaza los 4 botones planos por un único botón disparador con
     ícono de globo; al hacer click despliega un menú con las 4
     opciones (bandera + nombre completo). Misma lógica de fondo,
     solo cambia el marcado/visual.
  ──────────────────────────────────────────────────────────────────── */
  function renderLangButtons() {
    document.querySelectorAll('[id="langSwitcher"], .lang-switcher-inject').forEach(container => {
      if (container.dataset.rendered) return;
      container.dataset.rendered = '1';
      container.classList.add('lang-switcher');

      const current = localStorage.getItem('acmafer_lang') || 'es';

      /* Botón disparador */
      const trigger = document.createElement('button');
      trigger.type = 'button';
      trigger.className = 'lang-trigger';
      trigger.setAttribute('aria-haspopup', 'true');
      trigger.setAttribute('aria-expanded', 'false');
      trigger.title = 'Idioma / Language';
      trigger.innerHTML =
        '<i class="bi bi-globe2"></i>' +
        '<span class="lang-trigger-code">' + CODES[current] + '</span>' +
        '<i class="bi bi-chevron-down lang-trigger-chevron"></i>';

      /* Menú desplegable */
      const menu = document.createElement('div');
      menu.className = 'lang-menu';
      menu.setAttribute('role', 'menu');

      LANGS.forEach(lang => {
        const opt = document.createElement('button');
        opt.type = 'button';
        opt.className = 'lang-option';
        opt.dataset.lang = lang;
        opt.setAttribute('role', 'menuitem');
        opt.innerHTML =
          '<span class="lang-option-flag">' + FLAGS[lang] + '</span>' +
          '<span class="lang-option-name">' + NAMES[lang] + '</span>' +
          '<i class="bi bi-check2 lang-option-check"></i>';
        opt.addEventListener('click', (e) => {
          e.stopPropagation();
          window.acmSetLang(lang);
        });
        menu.appendChild(opt);
      });

      trigger.addEventListener('click', (e) => {
        e.stopPropagation();
        const isOpen = container.classList.toggle('lang-open');
        trigger.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
      });

      container.appendChild(trigger);
      container.appendChild(menu);
    });

    /* Cerrar al hacer click fuera de cualquier selector abierto */
    if (!window.__acmLangOutsideBound) {
      window.__acmLangOutsideBound = true;
      document.addEventListener('click', () => {
        document.querySelectorAll('.lang-switcher.lang-open').forEach(c => {
          c.classList.remove('lang-open');
          const t = c.querySelector('.lang-trigger');
          if (t) t.setAttribute('aria-expanded', 'false');
        });
      });
      document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
          document.querySelectorAll('.lang-switcher.lang-open').forEach(c => c.classList.remove('lang-open'));
        }
      });
    }
  }

  /* ── Init ──────────────────────────────────────────────────────────── */
  document.addEventListener('DOMContentLoaded', function () {
    renderLangButtons();
    const saved = localStorage.getItem('acmafer_lang') || 'es';
    applyLang(saved);
  });

})();