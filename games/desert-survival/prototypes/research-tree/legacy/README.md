# Legacy UI documents

`ResearchNodeRow.ui` is the row document of the tabular tree used by Research
0.1.0/0.1.1. The radial page (0.1.5+) never appends it. It is kept here, outside
`Common/`, so it is not packaged: its 0.1.2 revision introduced
`HorizontalAlignment: Right`, a value that no vanilla document uses (vanilla
only uses `Start`, `Center` and `End`), and every build that shipped it failed
the `dune1` join with `Could not find document Hud/ReturnToHubButton.ui`.
The copy here already uses `End`.
