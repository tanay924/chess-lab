<template>
  <div class="app-shell">
    <header class="topbar">
      <RouterLink class="brand" to="/">
        <span class="brand-mark">CL</span>
        <span>
          <strong>Chess Lab</strong>
          <small>Local Stockfish study</small>
        </span>
      </RouterLink>
      <nav>
        <RouterLink to="/">Import</RouterLink>
        <RouterLink to="/library">Library</RouterLink>
      </nav>
    </header>

    <main>
      <RouterView />
    </main>
  </div>
</template>
