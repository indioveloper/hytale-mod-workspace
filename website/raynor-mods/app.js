(function () {
  "use strict";

  const config = window.RAYNOR_PORTAL;
  const repository = config.links.github;

  document.querySelectorAll("[data-link]").forEach((link) => {
    link.href = config.links[link.dataset.link];
  });

  const iconArrow = '<span aria-hidden="true">↗</span>';
  const modsGrid = document.querySelector("#mods-grid");
  const packsList = document.querySelector("#packs-list");
  document.querySelector("#mod-count").textContent = config.mods.length;

  function projectUrl(path) {
    return `${repository}/tree/main/${path}`;
  }

  function renderMods(filter) {
    const selected = filter === "all"
      ? config.mods
      : config.mods.filter((mod) => mod.maturity === filter);

    modsGrid.innerHTML = selected.map((mod) => `
      <article class="mod-card accent-${mod.accent}" id="mod-${mod.slug}">
        <div class="mod-card-top">
          <img src="${mod.icon}" alt="" width="72" height="72">
          <div class="mod-version"><span>v${mod.version}</span><i class="status-${mod.maturity}">${mod.status}</i></div>
        </div>
        <h3>${mod.name}</h3>
        <p>${mod.summary}</p>
        <ul>${mod.features.map((feature) => `<li>${feature}</li>`).join("")}</ul>
        <div class="card-actions">
          <a href="${projectUrl(mod.source)}" target="_blank" rel="noreferrer">Código y documentación ${iconArrow}</a>
          ${mod.extraLink ? `<a class="secondary-action" href="${config.links[mod.extraLink.urlKey]}" target="_blank" rel="noreferrer">${mod.extraLink.label} ${iconArrow}</a>` : ""}
        </div>
      </article>
    `).join("");
  }

  function renderPacks() {
    packsList.innerHTML = config.packs.map((pack, index) => `
      <article class="pack-row">
        <div class="pack-index">${String(index + 1).padStart(2, "0")}</div>
        <div class="pack-marker" aria-hidden="true">${pack.marker}</div>
        <div class="pack-copy">
          <h3>${pack.name} <span>v${pack.version}</span></h3>
          <p>${pack.summary}</p>
        </div>
        <div class="pack-status">${pack.status}</div>
        <a href="${projectUrl(pack.source)}" target="_blank" rel="noreferrer" aria-label="Ver ${pack.name} en GitHub">Ver pack ${iconArrow}</a>
      </article>
    `).join("");
  }

  document.querySelectorAll(".filter").forEach((button) => {
    button.addEventListener("click", () => {
      document.querySelectorAll(".filter").forEach((candidate) => {
        const selected = candidate === button;
        candidate.classList.toggle("is-active", selected);
        candidate.setAttribute("aria-pressed", String(selected));
      });
      renderMods(button.dataset.filter);
    });
  });

  renderMods("all");
  renderPacks();
})();
