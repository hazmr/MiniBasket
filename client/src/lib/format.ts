const moneyFormat = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });
const dateFormat = new Intl.DateTimeFormat("en-US", { dateStyle: "medium", timeStyle: "short" });

export function money(amount: number) {
  return moneyFormat.format(amount);
}

export function date(value: string) {
  return dateFormat.format(new Date(value));
}

export function single(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}

export function text(formData: FormData, name: string): string {
  return String(formData.get(name) ?? "").trim();
}

export function optionalNumber(formData: FormData, name: string): number | null {
  const value = text(formData, name);
  return value === "" ? null : Number(value);
}
