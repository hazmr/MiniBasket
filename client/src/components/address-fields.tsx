import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";

export function AddressFields() {
  return (
    <>
      <Field>
        <FieldLabel htmlFor="street">Street</FieldLabel>
        <Input id="street" name="street" required maxLength={255} placeholder="12 Olive St" />
      </Field>
      <Field>
        <FieldLabel htmlFor="city">City</FieldLabel>
        <Input id="city" name="city" required maxLength={100} placeholder="Amman" />
      </Field>
    </>
  );
}
