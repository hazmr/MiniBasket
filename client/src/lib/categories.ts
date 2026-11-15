import type { Category } from "./types";

export function categoryLabel(category: Category, all: Category[]): string {
  const parent = all.find((candidate) => candidate.id === category.parentId);
  return parent ? `${categoryLabel(parent, all)} / ${category.name}` : category.name;
}
