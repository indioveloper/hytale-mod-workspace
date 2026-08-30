(function () {
  "use strict";

  const config = window.RAYNOR_PORTAL;
  const root = document.documentElement;
  const themeToggle = document.querySelector("#theme-toggle");
  const languageButtons = document.querySelectorAll("[data-language]");
  const params = new URLSearchParams(window.location.search);
  const requestedLanguage = params.get("lang");
  let currentLanguage = requestedLanguage === "en" || requestedLanguage === "es"
    ? requestedLanguage
    : (localStorage.getItem("raynor-language") === "en" ? "en" : "es");

  const translations = {
    es: {
      title: "Raynor Mods · Mods para Hytale",
      description: "Mods y herramientas WIP de Raynor para Hytale.",
      navMods: "Mods",
      navConfigurator: "Configurador",
      navPacks: "Asset packs",
      pageTitle: "Mods de Raynor",
      intro: "Proyectos en desarrollo para construir mapas, eventos y sistemas de juego.",
      webTool: "Herramienta web · WIP",
      configuratorTitle: "Configurador de Mob Spawners",
      configuratorDescription: "Configura un spawner en el navegador y copia el código resultante para importarlo dentro del juego.",
      openConfigurator: "Abrir configurador ↗",
      viewMod: "Ver mod",
      modsDescription: "Cada proyecto indica su versión y estado actual.",
      privateContent: "Contenido privado",
      darkMode: "Modo oscuro",
      lightMode: "Modo claro",
      enableDark: "Activar modo oscuro",
      enableLight: "Activar modo claro",
      curseforgeTitle: "Página de este mod en CurseForge",
      downloadMod: "Descargar JAR",
      configuratorButton: "Configurador"
    },
    en: {
      title: "Raynor Mods · Hytale Mods",
      description: "Work-in-progress Hytale mods and tools by Raynor.",
      navMods: "Mods",
      navConfigurator: "Configurator",
      navPacks: "Asset packs",
      pageTitle: "Raynor Mods",
      intro: "Work-in-progress projects for building maps, events and gameplay systems.",
      webTool: "Web tool · WIP",
      configuratorTitle: "Mob Spawner Configurator",
      configuratorDescription: "Configure a spawner in your browser and copy the resulting code to import it into the game.",
      openConfigurator: "Open configurator ↗",
      viewMod: "View mod",
      modsDescription: "Each project shows its current version and development status.",
      privateContent: "Private content",
      darkMode: "Dark mode",
      lightMode: "Light mode",
      enableDark: "Enable dark mode",
      enableLight: "Enable light mode",
      curseforgeTitle: "This mod's CurseForge page",
      downloadMod: "Download JAR",
      configuratorButton: "Configurator"
    }
  };

  if (localStorage.getItem("raynor-theme") === "dark") {
    root.dataset.theme = "dark";
  }

  function text(key) {
    return translations[currentLanguage][key];
  }

  function updateThemeLabel() {
    const dark = root.dataset.theme === "dark";
    themeToggle.textContent = dark ? text("lightMode") : text("darkMode");
    themeToggle.setAttribute("aria-label", dark ? text("enableLight") : text("enableDark"));
  }

  function renderMods() {
    const arrow = '<span aria-hidden="true">↗</span>';
    document.querySelector("#mods-list").innerHTML = config.mods.map((mod) => {
      const status = currentLanguage === "en" ? mod.statusEn : mod.status;
      const summary = currentLanguage === "en" ? mod.summaryEn : mod.summary;
      const configuratorLabel = currentLanguage === "en" ? mod.extraLink?.labelEn : mod.extraLink?.label;

      return `
        <article class="mod-row" id="mod-${mod.slug}">
          <img src="${mod.icon}" alt="" width="54" height="54">
          <div class="mod-copy">
            <div class="mod-heading">
              <h3>${mod.name}</h3>
              <span class="version">v${mod.version}</span>
              <span class="status">${status}</span>
            </div>
            <p>${summary}</p>
          </div>
          <div class="mod-actions">
            <a class="button download-link" href="${mod.download}" download>${text("downloadMod")} <span aria-hidden="true">↓</span></a>
            <a class="button curseforge-link" href="${mod.curseforge}" target="_blank" rel="noreferrer" title="${text("curseforgeTitle")}">CurseForge ${arrow}</a>
            ${mod.extraLink ? `<a class="button" href="${config.links[mod.extraLink.urlKey]}" target="_blank" rel="noreferrer">${configuratorLabel || text("configuratorButton")} ${arrow}</a>` : ""}
          </div>
        </article>
      `;
    }).join("");
  }

  function applyLanguage(language, updateUrl) {
    currentLanguage = language;
    root.lang = language;
    localStorage.setItem("raynor-language", language);
    document.title = text("title");
    document.querySelector('meta[name="description"]').content = text("description");
    document.querySelectorAll("[data-i18n]").forEach((element) => {
      element.textContent = text(element.dataset.i18n);
    });
    languageButtons.forEach((button) => {
      const active = button.dataset.language === language;
      button.classList.toggle("is-active", active);
      button.setAttribute("aria-pressed", String(active));
    });
    updateThemeLabel();
    renderMods();

    if (updateUrl) {
      const nextUrl = new URL(window.location.href);
      if (language === "en") {
        nextUrl.searchParams.set("lang", "en");
      } else {
        nextUrl.searchParams.delete("lang");
      }
      window.history.replaceState({}, "", nextUrl);
    }
  }

  themeToggle.addEventListener("click", () => {
    const dark = root.dataset.theme === "dark";
    if (dark) {
      delete root.dataset.theme;
      localStorage.setItem("raynor-theme", "light");
    } else {
      root.dataset.theme = "dark";
      localStorage.setItem("raynor-theme", "dark");
    }
    updateThemeLabel();
  });

  languageButtons.forEach((button) => {
    button.addEventListener("click", () => applyLanguage(button.dataset.language, true));
  });

  document.querySelectorAll("[data-link]").forEach((link) => {
    link.href = config.links[link.dataset.link];
  });

  applyLanguage(currentLanguage, false);
})();
