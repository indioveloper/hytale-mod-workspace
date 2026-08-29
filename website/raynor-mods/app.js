(function () {
  "use strict";

  const config = window.RAYNOR_PORTAL;
  const root = document.documentElement;
  const themeToggle = document.querySelector("#theme-toggle");
  const savedTheme = localStorage.getItem("raynor-theme");

  if (savedTheme === "dark") {
    root.dataset.theme = "dark";
  }

  function updateThemeLabel() {
    const dark = root.dataset.theme === "dark";
    themeToggle.textContent = dark ? "Modo claro" : "Modo oscuro";
    themeToggle.setAttribute("aria-label", dark ? "Activar modo claro" : "Activar modo oscuro");
  }

  updateThemeLabel();
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

  document.querySelectorAll("[data-link]").forEach((link) => {
    link.href = config.links[link.dataset.link];
  });

  const repository = config.links.github;
  const modsList = document.querySelector("#mods-list");
  const arrow = '<span aria-hidden="true">↗</span>';

  function projectUrl(path) {
    return `${repository}/tree/main/${path}`;
  }

  modsList.innerHTML = config.mods.map((mod) => `
    <article class="mod-row" id="mod-${mod.slug}">
      <img src="${mod.icon}" alt="" width="54" height="54">
      <div class="mod-copy">
        <div class="mod-heading">
          <h3>${mod.name}</h3>
          <span class="version">v${mod.version}</span>
          <span class="status">${mod.status}</span>
        </div>
        <p>${mod.summary}</p>
      </div>
      <div class="mod-actions">
        <a class="button curseforge-link" href="${mod.curseforge}" target="_blank" rel="noreferrer" title="Enlace provisional al perfil de Raynor en CurseForge">CurseForge ${arrow}</a>
        <a class="button" href="${projectUrl(mod.source)}" target="_blank" rel="noreferrer">GitHub ${arrow}</a>
        ${mod.extraLink ? `<a class="button" href="${config.links[mod.extraLink.urlKey]}" target="_blank" rel="noreferrer">${mod.extraLink.label} ${arrow}</a>` : ""}
      </div>
    </article>
  `).join("");
})();
