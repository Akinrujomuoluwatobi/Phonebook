package app.cansol.phonebook.Controllers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cansol.phonebook.Adapter.ContactListAdapter
import app.cansol.phonebook.ViewModel.ContactViewModel
import app.cansol.phonebook.Model.Contact
import app.cansol.phonebook.Network.Network
import app.cansol.phonebook.R

class MainActivity : AppCompatActivity() {
    lateinit var contactViewModel: ContactViewModel
    lateinit var contactAdapter: ContactListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recView = findViewById<RecyclerView>(R.id.contactRecyclerView)
        recView.layoutManager = LinearLayoutManager(this)
        val context = this

        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel::class.java)
        contactViewModel.getContact("1")
        if(Network.checkNetwork(this)) {
            contactViewModel.allContact.observe(this, object : Observer<List<Contact>> {
                override fun onChanged(t: List<Contact>?) {
                    contactAdapter = ContactListAdapter(context, t!!)
                    recView.adapter = contactAdapter
                    contactAdapter.notifyDataSetChanged()
                }
            })
        }
        else Toast.makeText(this, "Check network connection", Toast.LENGTH_SHORT).show()


        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val builder = AlertDialog.Builder(context).create()
                val dialogView = LayoutInflater.from(context).inflate(R.layout.contact_menu, null)
                val editView =
                    LayoutInflater.from(context).inflate(R.layout.edit_contact_form, null)
                builder.setView(dialogView)
                builder.show()
                dialogView.findViewById<TextView>(R.id.txteditCancel).setOnClickListener {
                    contactAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "nothing happened!", Toast.LENGTH_SHORT).show()
                    builder.dismiss()
                }

                dialogView.findViewById<TextView>(R.id.txtDelete).setOnClickListener {
                    contactViewModel.deleteContact(
                        contactAdapter.getContactAt(viewHolder.adapterPosition).id!!,
                        contactAdapter.getContactAt(viewHolder.adapterPosition).user_id
                    )

                    Toast.makeText(context, "Contact Deleted!", Toast.LENGTH_SHORT).show()
                    builder.dismiss()
                }
                dialogView.findViewById<TextView>(R.id.txtEdit).setOnClickListener {
                    Toast.makeText(context, "Contact !", Toast.LENGTH_SHORT).show()
                    builder.dismiss()

                    val builder = AlertDialog.Builder(context).create()
                    builder.setView(editView)
                    builder.show()

                    var contact = contactAdapter.getContactAt(viewHolder.adapterPosition)
                    val name = editView.findViewById<EditText>(R.id.txtEditName)
                    val number = editView.findViewById<EditText>(R.id.txtEditNumber)
                    name.setText(contact.contact_name)
                    number.setText(contact.contact_number)

                    editView.findViewById<TextView>(R.id.txtEditDone).setOnClickListener {


                        if (name.text.isNotEmpty() && number.text.isNotEmpty()) {
                            Toast.makeText(context, "${contact.contact_name}!", Toast.LENGTH_SHORT).show()
                            val cont = Contact(
                                contact.id,
                                contact.user_id,
                                name.text.toString(),
                                number.text.toString()
                            )
                            contactViewModel.updateContact(
                                cont,
                                contactAdapter.getContactAt(viewHolder.adapterPosition).user_id
                            )
                            builder.dismiss()
                        }
                        else{
                            contactAdapter.notifyDataSetChanged()
                            Toast.makeText(context, "Field should not be empty!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    editView.findViewById<TextView>(R.id.txtEditCancel).setOnClickListener {
                        contactAdapter.notifyDataSetChanged()
                        Toast.makeText(context, "nothing happened!", Toast.LENGTH_SHORT).show()
                        builder.dismiss()
                    }

                }
            }
        }).attachToRecyclerView(recView)


    }

    fun createContact(view: View) {
        if(Network.checkNetwork(this)) {
            val intent = Intent(this, CreateContactActivity::class.java)
            startActivityForResult(intent, 1)
        }
        else Toast.makeText(this, "Check network connection", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val name = data.getStringExtra("EXTRA_NAME")
            val number = data.getStringExtra("EXTRA_NUMBER")
            val contact = Contact("", "1", name, number)
            if(Network.checkNetwork(this)) {
                contactViewModel.createContact(contact, "1")
                Toast.makeText(this, "Contact Saved!", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(this, "Check network connection", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Contact not Saved!", Toast.LENGTH_SHORT).show()
        }

    }

}
